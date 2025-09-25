plugins {
    java
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "mafia.server"
version = "0.0.1-SNAPSHOT"
description = "mafia-data"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // jpa
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // redis
    api("org.springframework.boot:spring-boot-starter-data-redis")

    // db
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // querydsl
    api("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    api("io.github.openfeign.querydsl:querydsl-core:7.0")
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // test container
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.redis:testcontainers-redis")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "mafia.server"
            artifactId = "mafia-data"
            version = "0.0.1-SNAPSHOT"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Jar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = true
}

val generated = "src/main/generated"

sourceSets {
    main {
        java {
            srcDirs(generated)
        }
    }
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(file(generated))
}

tasks.named("clean") {
    delete(generated)
}