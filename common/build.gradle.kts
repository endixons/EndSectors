import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("io.lettuce:lettuce-core:7.1.0.RELEASE")
    implementation("io.netty:netty-all:4.2.7.Final")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.nats:jnats:2.20.0")
    implementation("org.mongodb:mongo-java-driver:3.12.14")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    compileOnly("com.mojang:authlib:1.5.21")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/**")

    dependencies {
        exclude(dependency("net.bytebuddy:.*"))
    }

    minimize {
        exclude(dependency("org.slf4j:.*"))
        exclude(dependency("com.fasterxml.jackson.core:.*"))
        exclude(dependency("io.lettuce:.*"))
    }

    manifest {
        attributes["Main-Class"] = "pl.endixon.sectors.common.app.AppBootstrap"
        attributes["Implementation-Title"] = "EndSectors-CommonApp"
        attributes["Implementation-Version"] = project.version
        attributes["Multi-Release"] = "true"
    }
}

tasks.build { dependsOn("shadowJar") }
tasks.assemble { dependsOn("shadowJar") }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}