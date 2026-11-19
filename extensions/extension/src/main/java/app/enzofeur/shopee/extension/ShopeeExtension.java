package app.enzofeur.shopee.extension;

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
}
