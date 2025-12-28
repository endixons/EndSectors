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
    implementation("io.lettuce:lettuce-core:7.2.1.RELEASE")
    implementation("io.netty:netty-all:4.2.7.Final")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("io.nats:jnats:2.24.1")
    implementation("org.mongodb:mongo-java-driver:3.12.14")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    implementation("ch.qos.logback:logback-classic:1.5.23")
    compileOnly("com.mojang:authlib:1.5.21")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    dependencies {
        exclude(dependency("net.bytebuddy:.*"))
    }

    minimize {
        exclude(dependency("org.slf4j:slf4j-api:.*"))
        exclude(dependency("ch.qos.logback:logback-classic:.*"))
        exclude(dependency("ch.qos.logback:logback-core:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
        exclude(dependency("ch.qos.logback:.*:.*"))
        exclude(dependency("org.fusesource.jansi:jansi:.*"))
    }

    manifest {
        attributes["Main-Class"] = "pl.endixon.sectors.common.app.AppBootstrap"
        attributes["Implementation-Title"] = "EndSectors-CommonApp"
        attributes["Implementation-Version"] = project.version.toString()
        attributes["Multi-Release"] = "true"
    }
}


tasks.build { dependsOn("shadowJar") }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}