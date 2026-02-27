plugins {
    kotlin("jvm") version "2.1.0"
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.github.ihrthk"
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
    vcsUrl.set("https://github.com/ihrthk/vivo-install-auto-confirm")
    plugins {
        create("vivoInstall") {
            id = "com.github.ihrthk.vivo-install-auto-confirm-plugin"
            implementationClass = "com.zhangls.vivo.install.VivoInstallPlugin"
            displayName = "Vivo ADB Auto Confirm Plugin"
            description = "自动安装 APK 到 vivo 设备的 Gradle 插件"
            tags.set(listOf("android", "adb", "vivo", "auto-install"))
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
