import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}




dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("io.lettuce:lettuce-core:7.1.0.RELEASE")
    implementation("io.netty:netty-all:4.2.7.Final")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")

    compileOnly("com.mojang:authlib:1.5.21")
    implementation("org.mongodb:mongo-java-driver:3.12.14")
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
}
