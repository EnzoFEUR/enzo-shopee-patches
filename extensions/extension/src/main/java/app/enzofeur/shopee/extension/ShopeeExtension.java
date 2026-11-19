package app.enzofeur.shopee.extension;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Runtime helpers injected into Shopee by Morphe patches.
 */
@SuppressWarnings("unused")
public final class ShopeeExtension {
    private static final String PRESET_AUTO = "auto";
    private static final String PRESET_LOWEST = "lowest";
    private static final String PRESET_FAST = "fast";
    private static final String PRESET_BALANCED = "balanced";
    private static final String PRESET_HIGHEST = "highest";

    private static final Set<String> HIDDEN_TAB_IDENTIFIERS = new HashSet<String>(Arrays.asList(
            "video_and_live",
            "live_and_video",
            "video",
            "shopee_video",
            "video_mixtab",
            "mixtab",
            "live",
            "live_streaming",
            "shopee_live"
    ));

    private ShopeeExtension() {
    }

    /**
     * Filters Live &amp; Video entries out of {@code BottomTabManager.getTabViewData()} in O(n)
     * without regex allocation, preserving the original list if fewer than two tabs would remain.
     */
    public static List<?> filterVideoAndLiveTabs(List<?> tabs) {
        if (tabs == null || tabs.size() <= 2) {
            return tabs;
        }

        ArrayList<Object> filtered = new ArrayList<Object>(tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            Object tab = tabs.get(i);
            if (!isVideoOrLiveTab(tab)) {
                filtered.add(tab);
            }
        }

        if (filtered.size() == tabs.size() || filtered.size() < 2) {
            return tabs;
        }

        return filtered;
    }

    static boolean isVideoOrLiveTab(Object tab) {
        if (tab == null) {
            return false;
        }

        String desc = String.valueOf(tab).toLowerCase(Locale.ROOT);
        return isHiddenToken(extractField(desc, "tabtype="))
                || isHiddenToken(extractField(desc, "tabid="));
    }

    private static boolean isHiddenToken(String value) {
        return value != null && HIDDEN_TAB_IDENTIFIERS.contains(value);
    }

    private static String extractField(String text, String key) {
        int start = text.indexOf(key);
        if (start < 0) {
            return null;
        }

        start += key.length();
        int comma = text.indexOf(',', start);
        int paren = text.indexOf(')', start);
        int end;
        if (comma >= 0 && paren >= 0) {
            end = Math.min(comma, paren);
        } else if (comma >= 0) {
            end = comma;
        } else if (paren >= 0) {
            end = paren;
        } else {
            end = text.length();
        }

        return text.substring(start, end).trim();
    }

    /**
     * Rewrites live LAS MPD manifest strings before native playback starts when {@code isLive} is
     * true; plain stream URLs and VOD playback pass through unchanged.
     */
    public static String rewriteLiveStreamSource(String source, boolean isLive, String presetConfig) {
        if (!isLive) {
            return source;
        }
        return rewriteLasManifest(source, presetConfig);
    }

    /**
     * Selects or locks a representation tier and optionally clears P2P flags inside a LAS MPD JSON
     * manifest according to {@code "<preset>:<lock>:<disableP2p>"}.
     */
    public static String rewriteLasManifest(String source, String presetConfig) {
        if (source == null || source.isEmpty() || presetConfig == null || presetConfig.isEmpty()) {
            return source;
        }

        if (!looksLikeLasManifest(source)) {
            return source;
        }

        String[] parts = parsePresetConfig(presetConfig);
        String preset = parts[0];
        boolean lockResolution = "1".equals(parts[1]);
        boolean disableP2p = "1".equals(parts[2]);

        if (PRESET_AUTO.equals(preset) && !disableP2p) {
            return source;
        }

        try {
            JSONObject root = new JSONObject(source);
            JSONArray adaptationSets = root.optJSONArray("adaptationSet");
            if (adaptationSets == null || adaptationSets.length() == 0) {
                return source;
            }

            boolean modified = false;
            for (int a = 0; a < adaptationSets.length(); a++) {
                JSONObject adaptationSet = adaptationSets.optJSONObject(a);
                if (adaptationSet == null) {
                    continue;
                }

                JSONArray reps = adaptationSet.optJSONArray("representation");
                if (reps == null || reps.length() == 0) {
                    continue;
                }

                List<Integer> validIndices = collectValidRepresentationIndices(reps);
                if (validIndices.isEmpty()) {
                    continue;
                }

                if (disableP2p && clearP2pFlags(reps, validIndices)) {
                    modified = true;
                }

                if (!PRESET_AUTO.equals(preset)) {
                    int chosenIndex = selectRepresentationIndex(reps, validIndices, preset);
                    if (chosenIndex >= 0 && applySelection(adaptationSet, reps, validIndices, chosenIndex, lockResolution)) {
                        modified = true;
                    }
                }
            }

            return modified ? root.toString() : source;
        } catch (Throwable ignored) {
            return source;
        }
    }

    private static boolean looksLikeLasManifest(String source) {
        int i = 0;
        int len = source.length();
        while (i < len && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        if (i >= len || source.charAt(i) != '{') {
            return false;
        }
        return source.contains("\"adaptationSet\"") && source.contains("\"representation\"");
    }

    private static String[] parsePresetConfig(String presetConfig) {
        String preset = PRESET_AUTO;
        String lock = "0";
        String disableP2p = "0";

        String[] raw = presetConfig.split(":", -1);
        if (raw.length >= 1 && raw[0] != null) {
            String candidate = raw[0].trim().toLowerCase(Locale.ROOT);
            if (PRESET_LOWEST.equals(candidate)
                    || PRESET_FAST.equals(candidate)
                    || PRESET_BALANCED.equals(candidate)
                    || PRESET_HIGHEST.equals(candidate)
                    || PRESET_AUTO.equals(candidate)) {
                preset = candidate;
            }
        }
        if (raw.length >= 2 && "1".equals(raw[1].trim())) {
            lock = "1";
        }
        if (raw.length >= 3 && "1".equals(raw[2].trim())) {
            disableP2p = "1";
        }
        return new String[] { preset, lock, disableP2p };
    }

    private static List<Integer> collectValidRepresentationIndices(JSONArray reps) {
        ArrayList<Integer> indices = new ArrayList<Integer>(reps.length());
        for (int i = 0; i < reps.length(); i++) {
            JSONObject rep = reps.optJSONObject(i);
            if (hasNonEmptyUrl(rep)) {
                indices.add(Integer.valueOf(i));
            }
        }
        return indices;
    }

    private static boolean hasNonEmptyUrl(JSONObject rep) {
        if (rep == null) {
            return false;
        }
        String url = rep.optString("url", "");
        return url != null && !url.trim().isEmpty();
    }

    private static boolean clearP2pFlags(JSONArray reps, List<Integer> validIndices) {
        boolean modified = false;
        for (int i = 0; i < validIndices.size(); i++) {
            JSONObject rep = reps.optJSONObject(validIndices.get(i).intValue());
            if (rep != null && (rep.optBoolean("enable_p2p", false) || rep.has("enable_p2p"))) {
                try {
                    rep.put("enable_p2p", false);
                    modified = true;
                } catch (Throwable ignored) {
                    // Preserve original entry if JSON mutation throws.
                }
            }
        }
        return modified;
    }

    private static boolean applySelection(
            JSONObject adaptationSet,
            JSONArray reps,
            List<Integer> validIndices,
            int chosenIndex,
            boolean lockResolution
    ) {
        JSONObject chosen = reps.optJSONObject(chosenIndex);
        if (chosen == null) {
            return false;
        }

        try {
            if (lockResolution) {
                chosen.put("defaultSelected", true);
                chosen.put("disabledFromAdaptive", false);
                JSONArray single = new JSONArray();
                single.put(chosen);
                adaptationSet.put("representation", single);
            } else {
                for (int i = 0; i < validIndices.size(); i++) {
                    int idx = validIndices.get(i).intValue();
                    JSONObject rep = reps.getJSONObject(idx);
                    rep.put("defaultSelected", idx == chosenIndex);
                }
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static int selectRepresentationIndex(JSONArray reps, List<Integer> validIndices, String preset) {
        if (PRESET_LOWEST.equals(preset)) {
            return findExtremeIndex(reps, validIndices, false);
        }
        if (PRESET_HIGHEST.equals(preset)) {
            return findExtremeIndex(reps, validIndices, true);
        }
        if (PRESET_FAST.equals(preset)) {
            int bestUnder540 = findHighestCappedIndex(reps, validIndices, 540, 900000);
            return bestUnder540 >= 0 ? bestUnder540 : findExtremeIndex(reps, validIndices, false);
        }
        if (PRESET_BALANCED.equals(preset)) {
            int bestUnder720 = findHighestCappedIndex(reps, validIndices, 720, 1800000);
            if (bestUnder720 >= 0) {
                return bestUnder720;
            }
            int closestTo720 = findClosestResolutionIndex(reps, validIndices, 720);
            return closestTo720 >= 0 ? closestTo720 : findExtremeIndex(reps, validIndices, false);
        }
        return -1;
    }

    private static int findExtremeIndex(JSONArray reps, List<Integer> validIndices, boolean pickHighest) {
        int bestIdx = -1;
        JSONObject bestRep = null;
        for (int i = 0; i < validIndices.size(); i++) {
            int idx = validIndices.get(i).intValue();
            JSONObject candidate = reps.optJSONObject(idx);
            if (candidate == null) {
                continue;
            }
            if (bestRep == null) {
                bestIdx = idx;
                bestRep = candidate;
                continue;
            }
            int cmp = compareQuality(candidate, bestRep);
            if ((pickHighest && cmp > 0) || (!pickHighest && cmp < 0)) {
                bestIdx = idx;
                bestRep = candidate;
            }
        }
        return bestIdx;
    }

    private static int findHighestCappedIndex(
            JSONArray reps,
            List<Integer> validIndices,
            int maxResolutionP,
            int maxBitrateFallback
    ) {
        int bestIdx = -1;
        JSONObject bestRep = null;
        for (int i = 0; i < validIndices.size(); i++) {
            int idx = validIndices.get(i).intValue();
            JSONObject candidate = reps.optJSONObject(idx);
            if (candidate == null) {
                continue;
            }
            int p = extractResolutionP(candidate);
            int b = extractBitrate(candidate);
            boolean withinCap = (p > 0 && p <= maxResolutionP)
                    || (p == 0 && b > 0 && b <= maxBitrateFallback);
            if (!withinCap) {
                continue;
            }
            if (bestRep == null || compareQuality(candidate, bestRep) > 0) {
                bestIdx = idx;
                bestRep = candidate;
            }
        }
        return bestIdx;
    }

    private static int findClosestResolutionIndex(JSONArray reps, List<Integer> validIndices, int targetP) {
        int bestIdx = -1;
        JSONObject bestRep = null;
        int bestDiff = Integer.MAX_VALUE;
        for (int i = 0; i < validIndices.size(); i++) {
            int idx = validIndices.get(i).intValue();
            JSONObject candidate = reps.optJSONObject(idx);
            if (candidate == null) {
                continue;
            }
            int p = extractResolutionP(candidate);
            if (p <= 0) {
                continue;
            }
            int diff = Math.abs(p - targetP);
            if (bestRep == null || diff < bestDiff || (diff == bestDiff && compareQuality(candidate, bestRep) < 0)) {
                bestIdx = idx;
                bestRep = candidate;
                bestDiff = diff;
            }
        }
        return bestIdx;
    }

    private static int compareQuality(JSONObject a, JSONObject b) {
        int pA = extractResolutionP(a);
        int pB = extractResolutionP(b);
        if (pA > 0 && pB > 0 && pA != pB) {
            return pA < pB ? -1 : 1;
        }
        int bA = extractBitrate(a);
        int bB = extractBitrate(b);
        if (bA != bB) {
            return bA < bB ? -1 : 1;
        }
        if (pA != pB) {
            return pA < pB ? -1 : 1;
        }
        return 0;
    }

    private static int extractResolutionP(JSONObject rep) {
        int w = rep.optInt("width", 0);
        int h = rep.optInt("height", 0);
        if (w > 0 && h > 0) {
            return Math.min(w, h);
        }
        if (h > 0) {
            return h;
        }

        String label = (rep.optString("qualityTypeName", "") + " " + rep.optString("qualityType", ""))
                .toLowerCase(Locale.ROOT);
        if (label.contains("1080") || label.contains("fhd") || label.contains("orig") || label.contains("source")) {
            return 1080;
        }
        if (label.contains("720") || label.contains("hd")) {
            return 720;
        }
        if (label.contains("540")) {
            return 540;
        }
        if (label.contains("480") || label.contains("sd")) {
            return 480;
        }
        if (label.contains("360") || label.contains("ld") || label.contains("smooth")) {
            return 360;
        }
        if (label.contains("240")) {
            return 240;
        }
        return 0;
    }

    private static int extractBitrate(JSONObject rep) {
        int maxBitrate = rep.optInt("maxBitrate", 0);
        return maxBitrate > 0 ? maxBitrate : rep.optInt("bitrate", 0);
    }
}
