import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("java")
    id("com.diffplug.spotless") version "8.1.0"
}

allprojects {
    group = "pl.endixon.sectors"
    version = "1.7.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
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
        options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }

    tasks.register("printVersion") {
        doLast {
            println(project.version)
        }
    }

    tasks.register<Copy>("collectAllJars") {
        group = "build"
        description = "Collects all module jars into build/all-jars with version cleaned up"

        from("common/build/libs") { include("*.jar") }
        from("paper/build/libs") { include("*.jar") }
        from("proxy/build/libs") { include("*.jar") }
        from("Tools/build/libs") { include("*.jar") }

        into("$buildDir/all-jars")

        rename { fileName ->
            fileName.replace("-SNAPSHOT", "")
        }
    }

    tasks.named("collectAllJars") {
        dependsOn(
            ":common:shadowJar",
            ":paper:shadowJar",
            ":proxy:shadowJar",
            ":Tools:shadowJar"
        )
    }
}
