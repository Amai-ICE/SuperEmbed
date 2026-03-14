plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.shadow)
    application
}

group = "uk.amaiice"
version = "1.0-SNAPSHOT"
val id = "superembed"

repositories {
    mavenCentral()

    //kord repositories
    maven("https://repo.kord.dev/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(libs.kord.core)

    implementation(libs.ktoml.core)
    implementation(libs.ktoml.file)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    // KtorのJSONシリアライズ用依存
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlin.serialization.json)

    //kordとかのログ処理用
    implementation(libs.slf4j.simple)
}

// Shadow時に正しく実行できるように、メインクラスの指定を行う。
application {
    mainClass.set("$group.$id.MainKt")
}

tasks.register("runBot") {

}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.AZUL
    }
}