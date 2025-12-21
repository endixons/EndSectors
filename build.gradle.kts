import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("java")
    id("com.diffplug.spotless") version "6.23.0"
    id("checkstyle")
    id("pmd")
}

allprojects {
    group = "pl.endixon.sectors"
    version = "1.5-BETA"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io/")
    }

    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")

    // Spotless – formatowanie Eclipse
    configure<SpotlessExtension> {
        java {
            eclipse().configFile(rootProject.file("config/intellij-java-formatter.xml"))

            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    checkstyle {
        toolVersion = "10.12.1"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    }

    pmd {
        toolVersion = "6.59.0"
        ruleSets = listOf(
            "category/java/bestpractices.xml",
            "category/java/design.xml",
            "category/java/errorprone.xml"
        )
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:all",
                "-Werror",
                "-parameters"
            )
        )
    }

    tasks.withType<Checkstyle>().configureEach {
        reports {
            xml.required.set(false)
            html.required.set(true)
        }
    }

    tasks.withType<Pmd>().configureEach {
        reports {
            xml.required.set(false)
            html.required.set(true)
        }
    }
}
