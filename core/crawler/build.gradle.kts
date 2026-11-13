import org.gradle.api.tasks.testing.Test

plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin { jvmToolchain(17) }

dependencies {
    api(project(":core:model"))
    implementation(libs.jsoup)
    testImplementation(libs.junit)
}

tasks.named<Test>("test") {
    useJUnit()
    testLogging { events("passed", "failed", "skipped") }
}
