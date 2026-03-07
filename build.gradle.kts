// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.20")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.50.0" apply false
    id("nl.littlerobots.version-catalog-update") version "0.8.4" apply false
    id("com.osacky.doctor") version "0.9.1" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.forkOptions.memoryMaximumSize = "2g"
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xjvm-default=all",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xcontext-receivers",
                "-Xskip-prerelease-check"
            )
            apiVersion = "1.9"
            languageVersion = "1.9"
        }
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
        maxHeapSize = "2g"
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}

subprojects {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                when (requested.group) {
                    "com.google.dagger" -> {
                        if (requested.name.startsWith("hilt-")) {
                            useVersion("2.50")
                        }
                    }
                    "org.jetbrains.kotlin" -> {
                        useVersion("1.9.20")
                    }
                    "org.jetbrains.kotlinx" -> {
                        when (requested.name) {
                            "kotlinx-coroutines-core" -> useVersion("1.7.3")
                            "kotlinx-coroutines-android" -> useVersion("1.7.3")
                            "kotlinx-coroutines-play-services" -> useVersion("1.7.3")
                            "kotlinx-serialization-json" -> useVersion("1.6.0")
                            "kotlinx-datetime" -> useVersion("0.4.1")
                        }
                    }
                    "androidx.core" -> {
                        if (requested.name == "core-ktx") {
                            useVersion("1.12.0")
                        }
                    }
                    "androidx.lifecycle" -> {
                        useVersion("2.7.0")
                    }
                    "androidx.room" -> {
                        useVersion("2.6.1")
                    }
                    "androidx.navigation" -> {
                        useVersion("2.7.6")
                    }
                    "com.squareup.retrofit2" -> {
                        useVersion("2.9.0")
                    }
                    "com.squareup.okhttp3" -> {
                        useVersion("4.12.0")
                    }
                }
            }
            
            force(
                "org.jetbrains.kotlin:kotlin-stdlib:1.9.20",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.20",
                "org.jetbrains.kotlin:kotlin-reflect:1.9.20",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3",
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3",
                "androidx.core:core-ktx:1.12.0",
                "androidx.appcompat:appcompat:1.6.1",
                "com.google.android.material:material:1.11.0"
            )
            
            cacheChangingModulesFor(0, "seconds")
            cacheDynamicVersionsFor(0, "seconds")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
    delete(file("${rootDir}/build"))
    delete(file("${rootDir}/.gradle"))
    delete(fileTree(rootDir) {
        include("**/build/")
        include("**/.cxx/")
        include("**/.externalNativeBuild/")
    })
}

tasks.withType<DependencyUpdatesTask> {
    resolutionStrategy {
        componentSelection {
            all {
                val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea", "eap", "dev", "snapshot")
                    .any { qualifier ->
                        candidate.version.contains(qualifier, ignoreCase = true)
                    }
                if (rejected) {
                    reject("Release candidate")
                }
            }
        }
    }
    checkForGradleUpdate = true
    outputFormatter = "html,xml"
    outputDir = "build/dependencyUpdates"
    reportfileName = "dependency-updates-report"
}

tasks.register("printVersionName") {
    doLast {
        println(project.version)
    }
}

tasks.register("printProjectInfo") {
    doLast {
        println("Project: ${rootProject.name}")
        println("Version: ${project.version}")
        println("Gradle Version: ${gradle.gradleVersion}")
        println("Kotlin Version: 1.9.20")
        println("Android Gradle Plugin: 8.2.2")
    }
}

tasks.register("lintProject") {
    group = "verification"
    description = "Runs lint on all modules"
    dependsOn(subprojects.map { it.tasks.named("lint") })
}

tasks.register("testProject") {
    group = "verification"
    description = "Runs tests on all modules"
    dependsOn(subprojects.map { it.tasks.named("test") })
}

tasks.register("checkProject") {
    group = "verification"
    description = "Runs all checks on all modules"
    dependsOn("lintProject", "testProject")
}

tasks.register("assembleProject") {
    group = "build"
    description = "Assembles all modules"
    dependsOn(subprojects.map { it.tasks.named("assemble") })
}

ext {
    set("compileSdkVersion", 34)
    set("minSdkVersion", 24)
    set("targetSdkVersion", 34)
    set("buildToolsVersion", "34.0.0")
    set("ndkVersion", "25.2.9519653")
    set("kotlinVersion", "1.9.20")
    set("coroutinesVersion", "1.7.3")
    set("roomVersion", "2.6.1")
    set("lifecycleVersion", "2.7.0")
    set("navigationVersion", "2.7.6")
    set("hiltVersion", "2.50")
    set("retrofitVersion", "2.9.0")
    set("okhttpVersion", "4.12.0")
    set("glideVersion", "4.16.0")
    set("timberVersion", "5.0.1")
}

tasks.register("generateLintBaseline") {
    group = "reporting"
    description = "Generate lint baseline for all modules"
    doLast {
        subprojects.forEach { project ->
            project.tasks.matching { it.name == "lint" }.configureEach {
                project.javaexec {
                    classpath = files()
                    mainClass.set("com.android.tools.lint.Main")
                    args = listOf(
                        "--baseline",
                        "${project.projectDir}/lint-baseline.xml",
                        "${project.projectDir}/src/main"
                    )
                }
            }
        }
    }
}
