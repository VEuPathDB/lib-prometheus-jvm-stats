import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  `java-library`
  `maven-publish`
  kotlin("jvm") version "1.6.10"
  id("org.jetbrains.dokka") version "1.6.10"
}

group = "org.veupathdb.lib"
version = "1.2.3"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("io.prometheus:simpleclient:0.14.1")
  implementation("io.prometheus:simpleclient_common:0.14.1")
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
  }
}

java {
  withJavadocJar()
  withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjvm-default=all")
  }
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets.configureEach {
    includeNonPublic.set(false)
    jdkVersion.set(8)
  }
}

publishing {
  repositories {
    maven {
      name = "GitHub"
      url = uri("https://maven.pkg.github.com/VEuPathDB/lib-prometheus-jvm-stats")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>("gpr") {
      from(components["java"])

      pom {
        name.set("Prometheus JVM Stats")
        description.set("Reports JVM stats to the default Prometheus java instance.")
        url.set("https://github.com/VEuPathDB/lib-prometheus-jvm-stats")
        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("epharper@upenn.edu")
            url.set("https://github.com/foxcapades")
            organization.set("VEuPathDB")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/VEuPathDB/lib-prometheus-jvm-stats.git")
          developerConnection.set("scm:git:ssh://github.com/VEuPathDB/lib-prometheus-jvm-stats.git")
          url.set("https://github.com/VEuPathDB/lib-prometheus-jvm-stats")
        }
      }
    }
  }
}
