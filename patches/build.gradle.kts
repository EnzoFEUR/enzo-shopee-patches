group = "app.enzofeur.shopee"

patches {
    about {
        name = "Enzo Shopee Patches"
        description = "Performance and ad/telemetry reduction patches for Shopee Android"
        source = "git@github.com:EnzoFEUR/enzo-shopee-patches.git"
        author = "EnzoFEUR"
        contact = "https://github.com/EnzoFEUR/enzo-shopee-patches/issues"
        website = "https://github.com/EnzoFEUR/enzo-shopee-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
    testImplementation(libs.junit)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
