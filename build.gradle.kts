plugins {
  `maven-publish`
  alias(libs.plugins.kotlin)
  alias(libs.plugins.dokka)
}

group = "org.veupathdb.lib"
version = "1.3.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.prometheus:simpleclient:0.16.0")
  implementation("io.prometheus:simpleclient_common:0.16.0")
}

kotlin {
  jvmToolchain(21)
}

java {
  withJavadocJar()
  withSourcesJar()
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
