import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(ClassPaths.ANDROID_GRADLE)
        classpath(ClassPaths.KOTLIN_GRADLE)
        classpath(ClassPaths.HILT_GRADLE)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        jitPack()
    }
}

