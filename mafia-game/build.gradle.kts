plugins {
    id("java")
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
}

group = "mafia.server"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":mafia-data"))
    implementation("org.springframework.boot:spring-boot-starter")

    // protobuf
    implementation("com.google.protobuf:protobuf-java:4.31.1")
    implementation("com.google.protobuf:protobuf-java-util:4.31.1")

    // netty
    implementation("io.netty:netty-all:4.1.127.Final")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.31.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                java {}
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDirs("../proto/game")
        }
    }
}

tasks.register("generateCSharpProto", Exec::class) {
    commandLine("../proto/game/generate_csharp.sh")
}

tasks.named("generateProto") {
    finalizedBy("generateCSharpProto")
}

tasks.test {
    useJUnitPlatform()
}