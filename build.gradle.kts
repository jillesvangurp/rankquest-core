plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases")
}


kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "1.8"
            }
        }
        val test by compilations.getting {
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "1.8"
            }
        }
    }
    js(IR) {
        nodejs {
            testTask( Action {
                useMocha {
                    // javascript is a lot slower than Java, we hit the default timeout of 2000
                    timeout = "20000"
                }
            })
        }
    }

    sourceSets {

        val commonMain by getting {
                dependencies {
                    api(KotlinX.coroutines.core)
                    api(Ktor.client.core)
                    api(KotlinX.datetime)
                    api(Ktor.client.serialization)
                    api("io.ktor:ktor-serialization-kotlinx-json:_")
                    api("com.jillesvangurp:search-client:_")

                }
            }

        val commonTest by getting {
                dependencies {
                    implementation(kotlin("test-common", "_"))
                    implementation(kotlin("test-annotations-common", "_"))
                    implementation(Testing.kotest.assertions.core)
                    implementation(KotlinX.coroutines.test)

                    implementation("com.github.jillesvangurp:querylight:_")
                }
            }

        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                runtimeOnly("org.junit.jupiter:junit-jupiter:_")
                implementation(kotlin("test-junit"))

                // kotlintest runner needs this to enable logging
                implementation("org.slf4j:slf4j-api:_")
                implementation("org.slf4j:jcl-over-slf4j:_")
                implementation("org.slf4j:log4j-over-slf4j:_")
                implementation("org.slf4j:jul-to-slf4j:_")
                implementation("ch.qos.logback:logback-classic:_")
                implementation(Ktor.client.cio)
                implementation(Ktor.server.core)
                implementation(Ktor.server.netty)
            }
        }

        val jsMain by getting {
                dependencies {
                }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}


publishing {
    repositories {
        maven {
            // GOOGLE_APPLICATION_CREDENTIALS env var must be set for this to work
            // public repository is at https://maven.tryformation.com/releases
            url = uri("gcs://mvn-public-tryformation/releases")
            name = "FormationPublic"
        }
    }
}
