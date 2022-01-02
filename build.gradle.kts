plugins {
    java
    `java-library`
    jacoco
    signing
    alias(libs.plugins.testlog)
    alias(libs.plugins.indra)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.indra.publishing.sonatype)
}

group = "dev.mardroemmar"
version = "0.1.0-SNAPSHOT"
description = "Library for common maps."

repositories {
    mavenCentral()
}

dependencies {
    api(libs.checker.qual)
    api(libs.apiguardian)

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.truth)
    testImplementation(libs.truth.java8)
}

val signingKey = System.getenv("SIGNING_KEY")
val signingPassword = System.getenv("SIGNING_PASSWORD")
if (signingKey != null && signingPassword != null) {
    signing.useInMemoryPgpKeys(signingKey, signingPassword)
}

indra {
    javaVersions {
        target(11)
    }

    github("Mardroemmar", "lexica") {
        ci(true)
    }

    license {
        spdx("MPL-2.0")
        name("Mozilla Public License 2.0")
        url("https://www.mozilla.org/en-US/MPL/2.0/")
    }

    configurePublications {
        pom {
            developers {
                developer {
                    id.set("Proximyst")
                    name.set("Mariell Hoversholm")
                    timezone.set("Europe/Stockholm")
                }
            }
        }
    }
}

indraSonatype {
    this.useAlternateSonatypeOSSHost("s01")
}

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.PLAIN_PARALLEL
}

configurations {
    testImplementation {
        exclude(group = "junit")
    }
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    javadoc {
        val opt = options as StandardJavadocDocletOptions
        opt.addStringOption("Xdoclint:none", "-quiet")

        opt.encoding("UTF-8")
        opt.charSet("UTF-8")
        doFirst {
            opt.links(
                "https://docs.oracle.com/en/java/javase/11/docs/api/",
            )
        }
    }
}
