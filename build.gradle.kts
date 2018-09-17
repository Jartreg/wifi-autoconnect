import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.netflix.gradle.plugins.deb.Deb
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.51"

    // Required for creating the package
    id("nebula.ospackage") version "4.10.0"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "me.jartreg"
version = "1.0-SNAPSHOT"
description = "Ein Service, um sich automatisch im Schul-WLAN anzumelden"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")

    // DBus Java Implementation
    compile("com.github.hypfvieh", "dbus-java", "2.7.5")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// The .jar file for distribution
val mainClass = "de.goetheschule_essen.technik.wifi_autoconnect.MainKt"
tasks.withType<ShadowJar> {
    baseName = "wifi-autoconnect"
    classifier = ""
    version = ""

    manifest {
        attributes(mapOf(
                "Main-Class" to mainClass
        ))
    }
}

// The .deb package
val servicesRoot = "/lib/systemd/system"
val serviceFilename = "wifi-autoconnect.service"
task("distDeb", Deb::class) {
    group = "distribution"

    packageName = "wifi-autoconnect"
    maintainer = "Technik-AG"
    release = "1"

    // Required packages
    requires("network-manager")
    requires("openjdk-9-jre-headless")

    // .jar file
    from(tasks.getByName("shadowJar").outputs.files) {
        rename { "wifi-autoconnect.jar" }
        into("/usr/share/java")
    }

    // Service configuration and installation
    from("./conf/$serviceFilename") {
        into(servicesRoot)
    }
    link("$servicesRoot/multi-user.target.wants/$serviceFilename", "$servicesRoot/$serviceFilename")

    // NetworkManager connectivity check configuration
    from("./conf/wifi-autoconnect-check.conf") {
        into("/etc/NetworkManager/conf.d")
    }

    // Configutation file
    from("./conf/wifi-autoconnect.properties") {
        into("/etc")

        // Make only root able to access the file
        fileMode = 384
    }
    configurationFile("/etc/wifi-autoconnect.properties")
}
tasks.getByName("assemble").dependsOn("distDeb")