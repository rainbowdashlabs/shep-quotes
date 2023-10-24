plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

    // database
    implementation("org.postgresql", "postgresql", "42.6.0")

    // Logging
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    implementation("org.apache.logging.log4j", "log4j-core", "2.21.0")
    implementation("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.21.0")
    implementation("club.minnced", "discord-webhooks", "0.8.4")

    implementation("de.chojo", "cjda-util", "2.7.8+beta.2"){
        exclude(group = "club.minnced", module = "opus-java")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    // utils
    implementation("org.apache.commons", "commons-lang3", "3.13.0")
    implementation("de.chojo", "sql-util", "1.5.0")
    implementation("de.chojo", "log-util", "1.0.1"){
        exclude(group="org.apache.logging.log4j")
    }
    implementation("com.google.guava", "guava", "32.1.3-jre")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
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
