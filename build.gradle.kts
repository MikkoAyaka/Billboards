plugins {
    java
    id ("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "de.blablubbabc"
version = "2.3.3"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.1")

    implementation("org.jetbrains:annotations:24.0.1")
    implementation("com.github.MrXiaoM:holoeasy:3.4.3-1")
}
tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("org.intellij.lang.annotations", "de.blablubbabc.billboards.util.annotations.intellij")
        relocate("org.jetbrains.annotations", "de.blablubbabc.billboards.util.annotations.jetbrains")
        relocate("org.holoeasy", "de.blablubbabc.billboards.util.holoeasy")
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
}
