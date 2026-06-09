plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Jetpack Compose 컴파일러 플러그인
}

android {
    namespace = "com.example.remarket_project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.remarket_project"
        minSdk = 24        // Android 7.0 이상 지원
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 출시 빌드 시 ProGuard 난독화 비활성(개발용)
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true      // Jetpack Compose UI 활성화 (스플래시 화면에 사용)
        viewBinding = true  // ViewBinding 활성화 — XML 레이아웃을 타입 안전하게 참조
    }
}

dependencies {

    // ── AndroidX 기본 ──────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)          // Kotlin 확장 함수 모음
    implementation(libs.androidx.appcompat)          // AppCompatActivity, 테마 하위 호환
    implementation(libs.androidx.constraintlayout)  // ConstraintLayout

    // ── 액티비티 / 생명주기 ──────────────────────────────────────────────────
    implementation(libs.androidx.activity.ktx)           // by viewModels() 등 KTX 확장
    implementation(libs.androidx.lifecycle.runtime.ktx)  // lifecycleScope 코루틴 지원
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel + viewModelScope

    // ── 스플래시 화면 ─────────────────────────────────────────────────────────
    implementation(libs.androidx.core.splashscreen) // Android 12+ SplashScreen API 하위 호환

    // ── Navigation Component ──────────────────────────────────────────────────
    implementation(libs.androidx.navigation.fragment.ktx) // NavHostFragment, findNavController
    implementation(libs.androidx.navigation.ui.ktx)       // setupWithNavController, AppBarConfiguration

    // ── RecyclerView / ViewPager2 ─────────────────────────────────────────────
    implementation(libs.androidx.recyclerview) // 목록 UI
    implementation(libs.viewpager2)            // 상품 이미지 슬라이더에 사용

    // ── Material Design ───────────────────────────────────────────────────────
    implementation(libs.material) // MaterialButton, NavigationView, BottomNavigationView 등

    // ── Jetpack Compose (스플래시 화면 전용) ──────────────────────────────────
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM — 버전 일괄 관리
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ── 네트워크 ──────────────────────────────────────────────────────────────
    implementation(libs.retrofit)          // REST API 클라이언트
    implementation(libs.retrofit.gson)     // Gson 변환기 — JSON ↔ Kotlin 데이터 클래스
    implementation(libs.okhttp.logging)    // HTTP 요청/응답 로그 (디버그용)

    // ── 이미지 로딩 ───────────────────────────────────────────────────────────
    implementation(libs.glide) // URL → ImageView 비동기 로딩

    // ── 테스트 ────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
