plugins {
    kotlin("jvm") version "1.3.61"
}

group = properties["pluginGroup"]!!
version = properties["pluginVersion"]!!

repositories {
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/") //paper
    maven(url = "https://repo.dmulloy2.net/nexus/repository/public/") //protocollib
    maven(url = "https://jitpack.io/") //tap, psychic
}

dependencies {
    implementation(kotlin("stdlib-jdk8")) //kotlin
    implementation("junit:junit:4.12") //junit
    implementation("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT") //paper
    implementation("com.comphenix.protocol:ProtocolLib:4.5.0") //protocollib
    implementation("com.github.noonmaru:tap:2.3.3") //tap
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
//    create<Copy>("distJar") {
//        from(jar)
//        into("W:\\Servers\\distServer\\plugins")
//    }
}