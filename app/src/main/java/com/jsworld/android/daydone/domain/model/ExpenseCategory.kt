package com.jsworld.android.daydone.domain.model

import androidx.annotation.StringRes
import com.jsworld.android.daydone.R

/**
 * 리포트용 지출 카테고리. 지출 입력에는 노출하지 않고,
 * 지출명 키워드 매칭으로 리포트 계산 시점에 파생된다.
 *
 * 라벨은 `@StringRes` 로만 들고 있는다(v1.5 국제화 3-5) — Int 상수라 순수 JVM
 * 테스트가 그대로 돌고, domain 에 Android 의존이 실질적으로 늘지 않는다.
 * 이모지는 번역 대상이 아니라 표식이라 그대로 둔다.
 */
enum class ExpenseCategory(@StringRes val labelRes: Int, val emoji: String) {
    PET(R.string.category_pet, "🐾"),
    OCCASION(R.string.category_occasion, "🎁"),
    TRAVEL(R.string.category_travel, "✈️"),
    EDUCATION(R.string.category_education, "📚"),
    SUBSCRIPTION(R.string.category_subscription, "📱"),
    HEALTH(R.string.category_health, "💊"),
    BEAUTY(R.string.category_beauty, "💇"),
    TRANSPORT(R.string.category_transport, "🚕"),
    ALCOHOL(R.string.category_alcohol, "🍺"),
    GROCERY(R.string.category_grocery, "🛒"),
    CAFE(R.string.category_cafe, "☕"),
    FOOD(R.string.category_food, "🍚"),
    FASHION(R.string.category_fashion, "👕"),
    LIVING(R.string.category_living, "🧺"),
    CULTURE(R.string.category_culture, "🎬"),
    ETC(R.string.category_etc, "💸")
}
