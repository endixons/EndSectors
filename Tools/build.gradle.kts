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
    compileOnly(project(":common"))
    compileOnly(project(":paper"))
    implementation("org.mongodb:mongo-java-driver:3.12.14")
    compileOnly("io.lettuce:lettuce-core:7.2.1.RELEASE")
    compileOnly("io.netty:netty-all:4.2.9.Final")
    compileOnly("com.google.code.gson:gson:2.13.2")
    compileOnly("fr.mrmicky:fastboard:2.1.5")
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.jar {
    archiveFileName.set("EndSectors-tools-base.jar")
    enabled = true
}

tasks.named<Jar>("sourcesJar") {
    archiveFileName.set("EndSectors-tools-sources.jar")
}

tasks.named<Jar>("javadocJar") {
    archiveFileName.set("EndSectors-tools-javadoc.jar")
}


tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveClassifier.set("")
    archiveFileName.set("EndSectors-tools.jar")
    exclude("META-INF/**")
    relocate("io.netty", "pl.endixon.sectors.shadow.netty")
    relocate("org.mongodb", "pl.endixon.sectors.shadow.mongodb")

    dependencies {
        exclude(dependency("net.bytebuddy:.*"))
    }

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