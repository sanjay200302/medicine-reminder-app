plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.medicine"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.medicine2027"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room components
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    // Lifecycle (ViewModel, LiveData)
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    // CardView
    implementation ("androidx.cardview:cardview:1.0.0")

    // NotificationCompat
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.core:core:1.12.0")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")

    // Java 8+ API desugaring (for time APIs if needed)
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}