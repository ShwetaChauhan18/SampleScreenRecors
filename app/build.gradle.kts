plugins {
    id(Plugins.ANDROID_APPLICATION)
    kotlin(Plugins.Kotlin.ANDROID)
    kotlin(Plugins.Kotlin.ANDROID_EXTENSIONS)
    kotlin(Plugins.Kotlin.KAPT)
    id(Plugins.HILT)
}

android {
    compileSdkVersion(Versions.COMPILE_SDK)
    buildToolsVersion(Versions.BUILD_TOOLS)

    defaultConfig {
        applicationId = App.ID
        minSdkVersion(Versions.MIN_SDK)
        targetSdkVersion(Versions.TARGET_SDK)
        versionCode = App.Version.CODE
        versionName = App.Version.NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    flavorDimensions(App.Dimension.DEFAULT)

    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(defaultFileTree())

    // Core
    implementation(Libs.CORE_KTX)

    // UI
    implementation(Libs.APPCOMPAT)
    implementation(Libs.CONSTRAINT_LAYOUT)
    implementation(Libs.RECYCLERVIEW)

    // Jetpack
    implementation(Libs.ACTIVITY_KTX)
    implementation(Libs.FRAGMENT_KTX)
    // ViewModel
    implementation(Libs.LIFECYCLE_VIEWMODEL_KTX)
    // LiveData
    implementation(Libs.LIFECYCLE_LIVEDATA_KTX)
    
    // Hilt
    implementation(Libs.HILT)
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    kapt(Libs.HILT_DAGGER_COMPILER)
    //kapt(Libs.HILT_COMPILER)

    // Material
    implementation(Libs.MATERIAL)

    // Coroutines
    implementation(Libs.COROUTINES_CORE)
    implementation(Libs.COROUTINES_ANDROID)

    // Gson
    implementation(Libs.GSON)

    // Timber
    implementation(Libs.TIMBER)

    // Easy permissions
    implementation(Libs.EASY_PERMISSIONS)

    implementation(Libs.LOCAL_BROADCAST)
    implementation(Libs.INTUIT_SSP)
    implementation(Libs.INTUIT_SDP)
    implementation(Libs.DOCUMENT_FILE)

    // Kotlin Reflect
    implementation(kotlin(Libs.REFLECT))
    
    // Glide
    implementation(Libs.GLIDE)
    // Unit testing
    testImplementation(Libs.JUNIT)
    testImplementation(Libs.JUNIT_EXT)
    testImplementation(Libs.ARCH_CORE_TESTING)
    testImplementation(Libs.COROUTINES_TEST)
    testImplementation(Libs.HAMCREST)
    testImplementation(Libs.MOCKITO_CORE)
    testImplementation(Libs.MOCKITO_KOTLIN)

    // UI testing
    androidTestImplementation(Libs.TEST_RUNNER)
    androidTestImplementation(Libs.JUNIT_EXT)
    androidTestImplementation(Libs.TEST_RULES)
    androidTestImplementation(Libs.ESPRESSO_CORE)
    androidTestImplementation(Libs.ESPRESSO_CONTRIB)
    
}
