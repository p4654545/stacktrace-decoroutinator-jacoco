import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stacktrace-decoroutinator-jvm-agent-lib"))
    implementation("net.bytebuddy:byte-buddy-agent:${properties["byteBuddyVersion"]}")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${properties["kotlinxCoroutinesVersion"]}")
    testImplementation("io.github.microutils:kotlin-logging-jvm:${properties["kotlinLoggingJvmVersion"]}")
    testRuntimeOnly("ch.qos.logback:logback-classic:${properties["logbackClassicVersion"]}")
}

val generateDecoroutinatorAgentBinTask = task("generateDecoroutinatorAgentBin") {
    dependsOn(":stacktrace-decoroutinator-jvm-agent:shadowJar")
    val folder = "$projectDir/src/main/resources"
    doLast {
        file(folder).mkdir()
        copy {
            from("${project(":stacktrace-decoroutinator-jvm-agent").buildDir}/libs")
            include("stacktrace-decoroutinator-jvm-agent-*-all.jar")
            into(folder)
            rename(".*", "decoroutinatorAgentJar.bin")
        }
    }
}

tasks.named("classes") {
    dependsOn(generateDecoroutinatorAgentBinTask)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Stacktrace-decoroutinator")
                description.set("Library for recovering stack trace in exceptions thrown in Kotlin coroutines.")
                url.set("https://stacktracedecoroutinator.reformator.dev")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Denis Berestinskii")
                        email.set("berestinsky@gmail.com")
                        url.set("https://github.com/Anamorphosee")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Anamorphosee/stacktrace-decoroutinator.git")
                    developerConnection.set("scm:git:ssh://github.com:Anamorphosee/stacktrace-decoroutinator.git")
                    url.set("http://github.com/Anamorphosee/stacktrace-decoroutinator/tree/master")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            val releaseRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) {
                snapshotRepoUrl
            } else {
                releaseRepoUrl
            }
            credentials {
                username = properties["sonatype.username"] as String?
                password = properties["sonatype.password"] as String?
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
