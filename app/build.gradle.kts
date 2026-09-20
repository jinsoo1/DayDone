import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// 배포용 서명 정보는 git에 올리지 않는 keystore.properties 에서 읽는다.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile")?.let { file(it).exists() } == true

android {
    namespace = "com.jsworld.android.daydone"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.jsworld.android.daydone"
        minSdk = 28
        targetSdk = 36
        versionCode = 10
        versionName = "1.4.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 담을 언어를 못 박는다. 이게 없으면 의존성이 끌고 온 수십 개 로케일이
        // 그대로 들어가고, 단말 언어가 일본어면 시스템 문구만 일본어로 섞인다.
        // 1.6.0 에서 "ja" 를 더한다.
        androidResources {
            localeFilters += listOf("ko")
        }
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // 릴리즈 앱과 동시에 설치되도록 패키지 ID 를 분리한다.
            // (이름은 src/debug/res 의 app_name 으로 구분)
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.lifecycle(
                    "[DayDone] keystore.properties 가 없어 release 서명이 설정되지 않았습니다. " +
                        "루트에 keystore.properties(storeFile/storePassword/keyAlias/keyPassword)를 만들어 주세요."
                )
            }
        }

        // QA: 릴리즈와 같은 R8/리소스 축소를 켜되, 별도 패키지로 나란히 설치된다.
        // 스토어 버전(실제 데이터)을 덮어쓰지 않고 축소 빌드를 검증하기 위한 용도.
        // 디버그 키로 서명하므로 실수로 스토어에 올릴 수 없다.
        create("qa") {
            initWith(getByName("release"))
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)

            freeCompilerArgs.addAll(
                "-Xcontext-receivers"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // ⚠️ HardcodedText 는 **XML 레이아웃 전용**이라 Compose 코드에선 한 건도 잡지
        // 못한다. 이 앱은 XML 화면이 없어서 실질 효과가 0이었다 — 추출 누락은
        // checkHardcodedKorean 태스크(아래)가 잡는다.
        enable += setOf("HardcodedText")
        // 번역이 없는 1.5.0 에선 MissingTranslation 이 의미가 없다. 1.6.0 에서 켠다.
        disable += setOf("MissingTranslation")
    }
}

dependencies {
    // Core & Lifecycle
    implementation(libs.android.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.work.runtime.ktx)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.material3)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.core)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.datastore.preferences)

    // 계산 로직 유닛 테스트 (순수 함수 UseCase)
    testImplementation(libs.junit)
}
/**
 * 문자열 추출 누락 검사 (docs/v1.5-design.md §3-1).
 *
 * Compose 코드엔 린트의 HardcodedText 가 안 먹으므로 직접 본다.
 * 주석을 뺀 Kotlin 소스에 한글 **문자열 리터럴**이 남아 있으면 실패한다.
 *
 * 허용 목록은 "번역하면 안 되는 것"과 "1.6.0 에서 재작성할 것"뿐이다.
 */
val hardcodedKoreanAllowList = listOf(
    // 번역이 아니라 재작성 대상 — 1.6.0
    "domain/usecase/ClassifyExpenseCategoryUseCase.kt",
    "domain/usecase/BuildMonthlyReportUseCase.kt",
    // 통화 접미사("원") 자체
    "presentation/util/MoneyFormat.kt"
)

tasks.register("checkHardcodedKorean") {
    group = "verification"
    description = "Kotlin 소스에 남은 한글 문자열 리터럴을 찾는다"
    doLast {
        val hangul = Regex("[\\uac00-\\ud7a3]")
        val lineComment = Regex("""^\s*(//|\*|/\*)""")
        val literal = Regex(""""[^"]*[\uac00-\ud7a3][^"]*"""")
        val hits = mutableListOf<String>()
        file("src/main/java").walkTopDown()
            .filter { it.extension == "kt" }
            .filter { f -> hardcodedKoreanAllowList.none { f.path.endsWith(it) } }
            .forEach { f ->
                f.readLines().forEachIndexed { i, line ->
                    if (lineComment.containsMatchIn(line)) return@forEachIndexed
                    if (line.contains("Log.")) return@forEachIndexed
                    val code = line.substringBefore("//")
                    if (literal.containsMatchIn(code) && hangul.containsMatchIn(code)) {
                        hits += "${f.path}:${i + 1}: ${line.trim()}"
                    }
                }
            }
        if (hits.isNotEmpty()) {
            throw GradleException(
                "추출되지 않은 한글 문자열 ${hits.size}건:\n" + hits.joinToString("\n")
            )
        }
        logger.lifecycle("[DayDone] 남은 한글 문자열 리터럴 없음")
    }
}
