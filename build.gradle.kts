plugins {
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = requireNotNull(properties["pluginGroup"]) { "Group is undefined in properties" }
version = requireNotNull(properties["pluginVersion"]) { "Version is undefined in properties" }

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://repo.dmulloy2.net/nexus/repository/public/")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
    compileOnly("com.github.noonmaru:tap:2.8.9")
    implementation("com.github.noonmaru:kommand:0.1.9")

    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.powermock:powermock-module-junit4:2.0.7")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.7")
    testImplementation("org.slf4j:slf4j-api:1.7.25")
    testImplementation("org.apache.logging.log4j:log4j-core:2.8.2")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.8.2")
    testImplementation("org.spigotmc:spigot:1.16.2-R0.1-SNAPSHOT")
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
    shadowJar {
        relocate("com.github.noonmaru.kommand", "${rootProject.group}.${rootProject.name}.shaded.kommand")
        archiveClassifier.set("dist")
    }
//    create<Copy>("distJar") {
//        from("shadowJar")
//        into("W:\\Servers\\server\\plugins")
//    }
}