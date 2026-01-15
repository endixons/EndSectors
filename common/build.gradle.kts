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
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("io.lettuce:lettuce-core:7.2.1.RELEASE")
    implementation("io.netty:netty-all:4.2.9.Final")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("io.nats:jnats:2.25.1")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.fusesource.jansi:jansi:2.4.2")
    implementation("ch.qos.logback:logback-classic:1.5.23")
}


tasks.jar {
    archiveFileName.set("EndSectors-application-base.jar")
    enabled = true
}

tasks.named<Jar>("sourcesJar") {
    archiveFileName.set("EndSectors-application-sources.jar")
}

tasks.named<Jar>("javadocJar") {
    archiveFileName.set("EndSectors-application-javadoc.jar")
}


tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveClassifier.set("")
    archiveFileName.set("EndSectors-application.jar")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("META-INF/maven/**", "META-INF/LICENSE*", "META-INF/NOTICE*")

    exclude("META-INF/native/libnetty_transport_native_kqueue_aarch_64.jnilib")
    exclude("META-INF/native/libnetty_transport_native_kqueue_x86_64.jnilib")
    exclude("META-INF/native/netty_transport_native_kqueue_x86_64.jnilib")
    exclude("META-INF/native/libnetty_transport_native_epoll_aarch_64.so")


    dependencies {
        exclude(dependency("net.bytebuddy:.*"))
    }

    minimize {
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