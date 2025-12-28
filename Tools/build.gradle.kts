import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
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
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("io.lettuce:lettuce-core:7.2.0.RELEASE")
    implementation("org.mongodb:mongo-java-driver:3.12.14")
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()

    exclude("META-INF/**")

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