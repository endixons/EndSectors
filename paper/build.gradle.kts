import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("io.github.goooler.shadow") version "8.1.8"
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
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("io.lettuce:lettuce-core:7.2.1.RELEASE")
    implementation("io.netty:netty-all:4.2.9.Final")
    implementation("com.google.code.gson:gson:2.13.2")

    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    implementation("fr.mrmicky:fastboard:2.1.5")
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

    relocate("fr.mrmicky.fastboard", "pl.endixon.sectors.shadow.fastboard") {
        include("fr.mrmicky.fastboard.**")
    }

    relocate("io.netty", "pl.endixon.sectors.shadow.netty") {
        include("io.netty.**")
    }


    dependencies {
        exclude(dependency("net.bytebuddy:.*"))
    }

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