plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
dependencies {
    implementation("io.ktor:ktor-server-core:3.1.2")
    implementation("io.ktor:ktor-server-netty:3.1.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.2")
    implementation("io.ktor:ktor-client-core:3.1.2")
    implementation("io.ktor:ktor-client-cio:3.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$1.8.1")
    implementation("io.ktor:ktor-server-cio:3.1.2")

}