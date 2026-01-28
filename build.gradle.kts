import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("java")
    id("com.diffplug.spotless") version "8.2.1"
}

allprojects {
    group = "pl.endixon.sectors"
    version = "1.7.4-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven ("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io/")
    }

    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        java {
            eclipse().configFile(rootProject.file("config/intellij-java-formatter.xml"))
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.matching { it.name.startsWith("spotless") }.configureEach {
        setEnabled(false)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}

tasks.register("printVersion") {
    doLast {
        println(project.version.toString())
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }

    tasks.withType<Javadoc> {
        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
        }
    }
}



    tasks.jar {
        enabled = false
    }


