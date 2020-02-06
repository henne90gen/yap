import org.gradle.internal.os.OperatingSystem

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.61"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    mavenCentral()
}

val lwjglVersion = "3.2.3"
val jomlVersion = "1.9.20"

val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX -> {
        val osArch = System.getProperty("os.arch")
        if (osArch.startsWith("arm") || osArch.startsWith("aarch64")) {
            val arch = if (osArch.contains("64") || osArch.startsWith("armv8")) "arm64" else "arm32"
            "natives-linux-$arch"
        } else {
            "natives-linux"
        }
    }
    OperatingSystem.MAC_OS -> {
        "natives-macos"
    }
    OperatingSystem.WINDOWS -> {
        if (System.getProperty("os.arch").contains("64")) "natives-windows" else "natives-windows-x86"
    }
    else -> ""
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // Align versions of all LWJGL components
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-bgfx")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-nanovg")
    implementation("org.lwjgl", "lwjgl-nuklear")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-par")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("org.lwjgl", "lwjgl-vulkan")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-bgfx", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-nanovg", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-nuklear", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-par", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
    implementation("org.joml", "joml", jomlVersion)

    // Logging
    implementation("org.apache.logging.log4j", "log4j-api", "2.13.0")
    implementation("org.apache.logging.log4j", "log4j-core", "2.13.0")
}

application {
    // Define the main class for the application.
    mainClassName = "de.yap.AppKt"
}
