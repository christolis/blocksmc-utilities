plugins {
    id("java-library")
    id("net.labymod.gradle")
    id("net.labymod.gradle.addon")
    id("com.diffplug.spotless") version "6.0.0"
}

repositories {
    mavenCentral()
}

group = "com.christolis"
version = System.getenv().getOrDefault("VERSION", "1.0.0")

labyMod {
    defaultPackageName = "com.christolis"
    addonInfo {
        namespace = "christolis"
        displayName = "BlocksMC Utilities"
        author = "Christolis"
        description = "LabyMod add-on which adds extra functionalities while playing on the BlocksMC server."
        minecraftVersion = "*"
        version = getVersion().toString()
    }

    minecraft {
        registerVersions(
                "1.8.9",
                "1.12.2",
                "1.16.5",
                "1.17.1",
                "1.18.2",
                "1.19.2",
                "1.19.3",
                "1.19.4",
                "1.20.1",
                "1.20.2",
                "1.20.4",
                "1.20.5",
                "1.20.6",
                "1.21"
        ) { version, provider ->
            configureRun(provider, version)
        }

        subprojects.forEach {
            if (it.name != "game-runner") {
                filter(it.name)
            }
        }
    }

    addonDev {
        productionRelease()
    }
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("net.labymod.gradle")
    plugins.apply("net.labymod.gradle.addon")
    plugins.apply("com.diffplug.spotless")

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }

    // All subprojects inherit root project group and version, to avoid duplication.
    group = rootProject.group
    version = rootProject.version

    spotless {
        java {
            targetExclude("build/**")
            endWithNewline()
            removeUnusedImports()
            eclipse().configFile("${rootProject.rootDir}/google-style-eclipse.xml")
        }
    }

}

fun configureRun(provider: net.labymod.gradle.core.minecraft.provider.VersionProvider, gameVersion: String) {
    provider.runConfiguration {
        mainClass = "net.minecraft.launchwrapper.Launch"
        jvmArgs("-Dnet.labymod.running-version=${gameVersion}")
        jvmArgs("-Dmixin.debug=true")
        jvmArgs("-Dnet.labymod.debugging.all=true")
        jvmArgs("-Dmixin.env.disableRefMap=true")

        args("--tweakClass", "net.labymod.core.loader.vanilla.launchwrapper.LabyModLaunchWrapperTweaker")
        args("--labymod-dev-environment", "true")
        args("--addon-dev-environment", "true")
    }

    provider.javaVersion = when (gameVersion) {
        else -> {
            JavaVersion.VERSION_21
        }
    }

    provider.mixin {
        val mixinMinVersion = when (gameVersion) {
            "1.8.9", "1.12.2", "1.16.5" -> {
                "0.6.6"
            }

            else -> {
                "0.8.2"
            }
        }

        minVersion = mixinMinVersion
    }
}
