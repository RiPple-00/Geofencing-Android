import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

// Release signing — read from gitignored keystore.properties (see keystore.properties.example).
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
// 서명하려면 네 속성이 모두 있고 keystore 파일이 실제로 존재해야 한다. 하나라도 빠지면 서명을
// 구성하지 않고 미서명으로 빌드(예: 시크릿 없는 CI) — 부분 설정으로 릴리스 빌드가 실패하지 않게.
val hasReleaseSigning = keystoreProperties.getProperty("storeFile")?.let { storeFileName ->
    keystoreProperties.getProperty("storePassword") != null &&
        keystoreProperties.getProperty("keyAlias") != null &&
        keystoreProperties.getProperty("keyPassword") != null &&
        rootProject.file(storeFileName).exists()
} ?: false

android {
    namespace = "com.example.geofencing"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.geofencing"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")

        // 에뮬레이터에서 호스트 PC(localhost)로 접근하는 특수 주소를 기본값으로 둔다.
        // 실기기/실제 BE 서버로 테스트할 때는 local.properties에 BASE_URL=http://<ip>:3000/api/ 를 추가하면 된다.
        val baseUrl = localProperties.getProperty("BASE_URL", "").ifBlank { "http://10.0.2.2:3000/api/" }
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        // Supabase (interim backend). Put SUPABASE_URL / SUPABASE_ANON_KEY in local.properties.
        // The anon (publishable) key is client-safe; never put the DB password or service_role key here.
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL", "").ifBlank { "https://localhost/" }
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        val supabaseKey = localProperties.getProperty("SUPABASE_ANON_KEY", "")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sign with the release key when keystore.properties is present; otherwise
            // the build stays unsigned (e.g. CI without secrets) rather than failing.
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
    implementation(libs.haze)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}