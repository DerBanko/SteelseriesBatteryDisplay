plugins {
    id("java")
    application
}

group = "tv.banko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.hid4java:hid4java:0.7.0")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}

tasks {
    withType<Jar> {
        val classpath = configurations.runtimeClasspath

        inputs.files(classpath).withNormalizer(ClasspathNormalizer::class.java)

        manifest {
            attributes["Main-Class"] = "tv.banko.batterydisplay.BatteryDisplay"

            attributes(
                    "Class-Path" to classpath.map { cp -> cp.joinToString(" ") { "./lib/" + it.name } }
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

application {
    mainClass.set("tv.banko.batterydisplay.BatteryDisplay")
}