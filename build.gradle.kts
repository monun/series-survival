plugins {
    kotlin("jvm") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "5.2.0"
//    id("de.undercouch.download") version "4.1.1"
//    `maven-publish`
}

val relocate = (findProperty("relocate") as? String)?.toBoolean() ?: true

println("relocate = $relocate")

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.4-R0.1-SNAPSHOT")

    implementation("com.github.noonmaru:tap:3.2.6")
    implementation("com.github.noonmaru:kommand:0.6.3")

//    testImplementation("junit:junit:4.13")
//    testImplementation("org.mockito:mockito-core:3.3.3")
//    testImplementation("org.powermock:powermock-module-junit4:2.0.7")
//    testImplementation("org.powermock:powermock-api-mockito2:2.0.7")
//    testImplementation("org.slf4j:slf4j-api:1.7.25")
//    testImplementation("org.apache.logging.log4j:log4j-core:2.8.2")
//    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.8.2")
//    testImplementation("org.spigotmc:spigot:1.16.3-R0.1-SNAPSHOT")
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
    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
    shadowJar {
        archiveBaseName.set(project.property("pluginName").toString())
        archiveVersion.set("") // For bukkit plugin update
        archiveClassifier.set("") // Remove 'all'

        if (relocate) {
            relocate("com.github.noonmaru.kommand", "${rootProject.group}.${rootProject.name}.kommand")
            relocate("com.github.noonmaru.tap", "${rootProject.group}.${rootProject.name}.tap")
        }
    }
    create<Copy>("docker") {
        from(shadowJar)
        var dest = File(".docker/plugins")
        if (File(dest, shadowJar.get().archiveFileName.get()).exists())
            dest = File(dest, "update") // if plugin.jar exists in plugins change dest to plugins/update
        into(dest)
    }

    build {
        dependsOn(shadowJar)
    }

//    val buildtoolsDir = ".buildtools/"
//
//    create<de.undercouch.gradle.tasks.download.Download>("downloadBuildTools") {
//        src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
//        dest("$buildtoolsDir/BuildTools.jar")
//    }
//    create<DefaultTask>("setupWorkspace") {
//        doLast {
//            for (v in listOf("1.16.3")) {
//                javaexec {
//                    workingDir(buildtoolsDir)
//                    main = "-jar"
//                    args = listOf(
//                        "./BuildTools.jar",
//                        "--rev",
//                        v
//                    )
//                }
//            }
//            File(buildtoolsDir).deleteRecursively()
//        }
//
//        dependsOn(named("downloadBuildTools"))
//    }
}

//publishing {
//    publications {
//        create<MavenPublication>("Sample") {
//            from(components["java"])
//            artifact(tasks["sourcesJar"])
//        }
//    }
//}