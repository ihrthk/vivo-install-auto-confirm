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
        // 为每个插件自动创建 plugin marker publication
        // Gradle 会自动生成 plugin marker POM
        withType<MavenPublication>().configureEach {
            // 修正 artifactId 为 Gradle 期望的格式
            if (name == "pluginMaven") {
                artifactId = "gradle-vivo-plugin"
            }
        }
    }
}

// ============================================
// 插件标记生成
// ============================================
tasks.register("createPluginMarkerPom") {
    val pomFile = file("$buildDir/plugin-marker/pom.xml")
    outputs.file(pomFile)

    doLast {
        pomFile.parentFile.mkdirs()
        pomFile.writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.github.ihrthk</groupId>
                <artifactId>com.github.ihrthk.vivo-install-auto-confirm-plugin.gradle.plugin</artifactId>
                <version>${project.version}</version>
                <packaging>pom</packaging>
                <name>Vivo ADB Auto Confirm Plugin</name>
                <description>自动安装 APK 到 vivo 设备的 Gradle 插件</description>
                <url>https://github.com/ihrthk/vivo-install-auto-confirm</url>

                <licenses>
                    <license>
                        <name>MIT License</name>
                        <url>https://opensource.org/licenses/MIT</url>
                    </license>
                </licenses>

                <developers>
                    <developer>
                        <id>ihrthk</id>
                        <name>zhangls</name>
                        <email>ihrthk@163.com</email>
                    </developer>
                </developers>

                <scm>
                    <connection>scm:git:git://github.com/ihrthk/vivo-install-auto-confirm.git</connection>
                    <developerConnection>scm:git:ssh://github.com:ihrthk/vivo-install-auto-confirm.git</developerConnection>
                    <url>https://github.com/ihrthk/vivo-install-auto-confirm</url>
                </scm>

                <dependencies>
                    <dependency>
                        <groupId>com.github.ihrthk.vivo-install-auto-confirm</groupId>
                        <artifactId>vivo-install-auto-confirm</artifactId>
                        <version>${project.version}</version>
                    </dependency>
                </dependencies>
            </project>
        """.trimIndent())
    }
}

// Maven Local 发布
tasks.register("publishPluginMarkerToMavenLocal") {
    dependsOn("createPluginMarkerPom")
    doLast {
        val pomFile = file("$buildDir/plugin-marker/pom.xml")
        val targetDir = file("${System.getProperty("user.home")}/.m2/repository/com/github/ihrthk/com.github.ihrthk.vivo-install-auto-confirm-plugin.gradle.plugin/${project.version}")
        targetDir.mkdirs()
        pomFile.copyTo(File(targetDir, "com.github.ihrthk.vivo-install-auto-confirm-plugin.gradle.plugin-${project.version}.pom"), overwrite = true)
    }
}

tasks.named("publishToMavenLocal") {
    dependsOn("publishPluginMarkerToMavenLocal")
}

// JitPack 发布：生成适配 JitPack groupId 的插件标记
tasks.register("jitpackPublish") {
    dependsOn("createPluginMarkerPom", "build")
    doLast {
        // JitPack 会将 groupId 改为 com.github.ihrthk.vivo-install-auto-confirm
        val sourcePom = file("$buildDir/plugin-marker/pom.xml")
        val targetDir = file("$buildDir/repo/com/github/ihrthk/vivo-install-auto-confirm/com.github.ihrthk.vivo-install-auto-confirm-plugin.gradle.plugin/${project.version}")
        targetDir.mkdirs()

        val modifiedPom = sourcePom.readText()
            .replace("<groupId>com.github.ihrthk</groupId>", "<groupId>com.github.ihrthk.vivo-install-auto-confirm</groupId>")

        File(targetDir, "com.github.ihrthk.vivo-install-auto-confirm-plugin.gradle.plugin-${project.version}.pom")
            .writeText(modifiedPom)
    }
}
