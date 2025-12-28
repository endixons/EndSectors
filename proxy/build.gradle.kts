import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("io.lettuce:lettuce-core:7.1.0.RELEASE")
    implementation("io.netty:netty-all:4.2.9.Final")
    implementation("com.google.code.gson:gson:2.13.2")
    compileOnly("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("com.mojang:authlib:1.5.21")
    implementation("io.nats:jnats:2.24.1")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    minimize()
    dependencies {
        exclude(dependency("net.bytebuddy:.*"))
    }

    relocate("io.netty", "pl.endixon.sectors.shadow.netty") {
        include("io.netty.**")
    }

    exclude("META-INF/**")
    exclude(
        "**/org/bukkit/**",
        "**/io/papermc/**",
        "**/net/md_5/bungee/**",
        "**/pl/endixon/sectors/paper/**"
    )
}



tasks.build {
    dependsOn(tasks.named("shadowJar"))
}
tasks.assemble {
    dependsOn(tasks.named("shadowJar"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:none")
}
