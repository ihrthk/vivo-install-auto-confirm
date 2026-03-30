plugins {
    kotlin("jvm") version "2.1.0"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.0"
}

group = "io.github.ihrthk"
version = "1.0.3"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.1.0")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website.set("https://github.com/ihrthk/vivo-install-auto-confirm")
    vcsUrl.set("https://github.com/ihrthk/vivo-install-auto-confirm.git")
    plugins {
        create("vivoInstall") {
            id = "io.github.ihrthk.vivo-install-auto-confirm-plugin"
            implementationClass = "com.zhangls.vivo.install.VivoInstallPlugin"
            displayName = "Vivo ADB Auto Confirm Plugin"
            description = "自动安装 APK 到 vivo 设备的 Gradle 插件，支持自动确认安装弹窗和启动应用"
            tags.set(listOf("android", "adb", "vivo", "auto-install", "automation"))
        }
    }
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            if (name == "pluginMaven") {
                artifactId = "gradle-vivo-plugin"
            }
        }
    }
}
