import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.mikeprimm.com/")
    maven {
        name = "lunarclient"
        url = uri("https://repo.lunarclient.dev")
    }
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
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("io.lettuce:lettuce-core:7.1.0.RELEASE")
    implementation("io.netty:netty-all:4.2.7.Final")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.mongodb:mongo-java-driver:3.12.14")
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    implementation("fr.mrmicky:fastboard:2.1.5")
    compileOnly("com.lunarclient:apollo-api:1.2.1")
    compileOnly("com.lunarclient:apollo-extra-adventure4:1.2.1")

}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
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
