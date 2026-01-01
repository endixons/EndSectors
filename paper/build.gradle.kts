import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.mikeprimm.com/")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(project(":common")) {
        exclude(group = "ch.qos.logback")
        exclude(group = "org.slf4j")
        exclude(group = "org.fusesource.jansi")
    }

    compileOnly("io.lettuce:lettuce-core:7.2.1.RELEASE")
    compileOnly("io.netty:netty-all:4.2.9.Final")
    compileOnly("com.google.code.gson:gson:2.13.2")
    compileOnly("fr.mrmicky:fastboard:2.1.5")
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.jar {
    archiveFileName.set("EndSectors-paper-base.jar")
    enabled = true
}

tasks.named<Jar>("sourcesJar") {
    archiveFileName.set("EndSectors-paper-sources.jar")
}

tasks.named<Jar>("javadocJar") {
    archiveFileName.set("EndSectors-paper-javadoc.jar")
}


tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveClassifier.set("")
    archiveFileName.set("EndSectors-paper.jar")
    exclude("META-INF/**")

    relocate("fr.mrmicky.fastboard", "pl.endixon.sectors.shadow.fastboard")
    relocate("io.netty", "pl.endixon.sectors.shadow.netty")
    relocate("io.lettuce", "pl.endixon.sectors.shadow.lettuce")
    relocate("com.google.gson", "pl.endixon.sectors.shadow.gson")

    minimize()
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