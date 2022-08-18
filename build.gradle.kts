plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
    `maven-publish`
}

group = "de.chojo"
version = "1.0.0"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("de.chojo", "cjda-util", "2.7.0+alpha.17-SNAPSHOT")

    // database
    implementation("org.postgresql", "postgresql", "42.4.0")

    // Logging
    implementation("org.slf4j", "slf4j-api", "1.7.36")
    implementation("org.apache.logging.log4j", "log4j-core", "2.18.0")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", "2.18.0")
    implementation("club.minnced", "discord-webhooks", "0.8.2")

    // utils
    implementation("org.apache.commons", "commons-lang3", "3.12.0")
    implementation("de.chojo.sadu", "sadu-queries", "1.0.0-DEV")
    implementation("de.chojo.sadu", "sadu-postgresql", "1.0.0-DEV")
    implementation("de.chojo.sadu", "sadu-datasource", "1.0.0-DEV")
    implementation("de.chojo.sadu", "sadu-updater", "1.0.0-DEV")
    implementation("de.chojo", "log-util", "1.0.0")
    implementation("com.google.guava", "guava", "31.1-jre")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                expand(
                    "version" to project.version
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.shepquotes.ShepQuotes"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
