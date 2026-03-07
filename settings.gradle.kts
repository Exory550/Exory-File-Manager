pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        id("com.android.application") version "8.2.2" apply false
        id("com.android.library") version "8.2.2" apply false
        id("org.jetbrains.kotlin.android") version "1.9.20" apply false
        id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false
        id("org.jetbrains.kotlin.kapt") version "1.9.20" apply false
        id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
        id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.20" apply false
        id("org.jetbrains.compose") version "1.5.11" apply false
        id("com.google.dagger.hilt.android") version "2.50" apply false
        id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
        id("androidx.navigation.safeargs.kotlin") version "2.7.6" apply false
        id("com.google.gms.google-services") version "4.4.0" apply false
        id("com.google.firebase.crashlytics") version "2.9.9" apply false
        id("com.google.firebase.firebase-perf") version "1.4.2" apply false
        id("com.google.android.gms.oss-licenses-plugin") version "0.10.6" apply false
        id("com.github.ben-manes.versions") version "0.50.0" apply false
        id("nl.littlerobots.version-catalog-update") version "0.8.4" apply false
        id("com.osacky.doctor") version "0.9.1" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo1.maven.org/maven2/")
        maven("https://jcenter.bintray.com/") {
            content {
                includeGroup("org.webkit")
                includeGroup("com.github.tony19")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://api.mapbox.com/downloads/v2/releases/maven")
            }
            filter {
                includeGroup("com.mapbox.mapboxsdk")
                includeGroup("com.mapbox.common")
            }
        }
    }
    
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "ExoryFileManager"
include(":app")

// Build cache configuration
buildCache {
    local {
        isEnabled = true
        removeUnusedEntriesAfterDays = 30
    }
}

// Performance optimizations
gradle.projectsLoaded {
    gradle.rootProject {
        extra.set("android.useAndroidX", true)
        extra.set("android.enableJetifier", true)
        extra.set("kotlin.code.style", "official")
        extra.set("org.gradle.parallel", true)
        extra.set("org.gradle.caching", true)
        extra.set("org.gradle.configureondemand", true)
        extra.set("org.gradle.daemon", true)
        extra.set("org.gradle.jvmargs", "-Xmx4096m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8")
        extra.set("kotlin.incremental", true)
        extra.set("kotlin.incremental.java", true)
        extra.set("kotlin.incremental.js", true)
        extra.set("kotlin.parallel.tasks.in.project", true)
        extra.set("kotlin.mpp.stability.nowarn", true)
        extra.set("kapt.incremental.apt", true)
        extra.set("kapt.use.worker.api", true)
        extra.set("kapt.include.compile.classpath", false)
    }
}

// Dependency resolution strategy
configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
        cacheDynamicVersionsFor(0, "seconds")
        
        eachDependency {
            when (requested.group) {
                "org.jetbrains.kotlin" -> {
                    useVersion("1.9.20")
                }
                "org.jetbrains.kotlinx" -> {
                    when (requested.name) {
                        "kotlinx-coroutines-core" -> useVersion("1.7.3")
                        "kotlinx-coroutines-android" -> useVersion("1.7.3")
                        "kotlinx-serialization-json" -> useVersion("1.6.0")
                    }
                }
                "androidx.core" -> {
                    if (requested.name == "core-ktx") {
                        useVersion("1.12.0")
                    }
                }
                "com.google.dagger" -> {
                    if (requested.name.startsWith("hilt-")) {
                        useVersion("2.50")
                    }
                }
            }
        }
    }
}

// Plugin validation
plugins {
    id("com.osacky.doctor") version "0.9.1" apply false
}

doctor {
    disallowMultipleDaemons.set(true)
    disallowSyncExecution.set(true)
    warnWhenNotUsingParallelGC.set(true)
    javaHome {
        ensureJavaHomeMatches.set(true)
        ensureJavaHomeIsValid.set(true)
    }
}
