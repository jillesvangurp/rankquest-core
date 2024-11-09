import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    js(IR) {
        browser {
            testTask {
                useMocha {
                    // javascript is a lot slower than Java, we hit the default timeout of 2000
                    timeout = "30s"
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    // javascript is a lot slower than Java, we hit the default timeout of 2000
                    timeout = "30s"
                }
            }
        }
    }
    // needs kt-search to add the target
//    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    // iOS targets
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useMocha {
                    // javascript is a lot slower than Java, we hit the default timeout of 2000
                    timeout = "30s"
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    // javascript is a lot slower than Java, we hit the default timeout of 2000
                    timeout = "30s"
                }
            }
        }
        // errors
//        d8 {
//            testTask {
//                useMocha {
//                    // javascript is a lot slower than Java, we hit the default timeout of 2000
//                    timeout = "30s"
//                }
//            }
//        }
    }

    sourceSets {

        commonMain {
                dependencies {
                    api(KotlinX.coroutines.core)
                    api(Ktor.client.core)
                    api(KotlinX.datetime)
                    api(Ktor.client.serialization)
                    api("io.ktor:ktor-serialization-kotlinx-json:_")
                    api("com.jillesvangurp:search-client:_")

                }
            }

        commonTest {
                dependencies {
                    implementation(kotlin("test-common", "_"))
                    implementation(kotlin("test-annotations-common", "_"))
                    implementation(Testing.kotest.assertions.core)
                    implementation(KotlinX.coroutines.test)

                    implementation("com.github.jillesvangurp:querylight:_")
                }
            }

        jvmMain {
            dependencies {
            }
        }
        jvmTest {
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

        jsMain {
                dependencies {
                }
        }

        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        wasmJsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js-wasm-js:_")
            }
        }

        wasmJsTest {
            dependencies {
                implementation(kotlin("test-wasm-js"))
            }
        }

        iosMain {
            dependencies {
                implementation(Ktor.client.darwin)
            }
        }

        macosMain {
            dependencies {
                implementation(Ktor.client.darwin)
            }
        }

        mingwMain {
            dependencies {
                implementation(Ktor.client.curl)
            }
        }
//        linuxMain {
//            dependencies {
//                implementation(Ktor.client.curl)
//            }
//        }

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                languageVersion = "1.9"
                apiVersion = "1.9"
            }
        }
    }
}

tasks.named("iosSimulatorArm64Test") {
    // requires IOS simulator and tens of GB of other stuff to be installed
    // so keep it disabled
    enabled = false
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
