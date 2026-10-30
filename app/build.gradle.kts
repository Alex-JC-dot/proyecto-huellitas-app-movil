import java.util.Properties

// Lee local.properties (ese archivo NO se sube a GitHub: ahí van datos privados como la URL del webhook)
val localProps = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.AlexYChristian.ProyectoSis145"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.AlexYChristian.ProyectoSis145"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL del webhook de Make. Se toma de local.properties (MAKE_WEBHOOK_URL=...)
        // y queda disponible en Kotlin como BuildConfig.MAKE_WEBHOOK_URL.
        // Si no está definida, queda "" y la app simplemente no envía nada.
        buildConfigField(
            "String",
            "MAKE_WEBHOOK_URL",
            "\"${localProps.getProperty("MAKE_WEBHOOK_URL", "")}\""
        )
        // Clave que el filtro 🔐 del escenario de Make exige en cada envío (campo "clave_app")
        buildConfigField(
            "String",
            "MAKE_CLAVE_APP",
            "\"${localProps.getProperty("MAKE_CLAVE_APP", "")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room (base de datos local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // OkHttp: cliente HTTP para enviar datos al webhook de Make
    implementation(libs.squareup.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}