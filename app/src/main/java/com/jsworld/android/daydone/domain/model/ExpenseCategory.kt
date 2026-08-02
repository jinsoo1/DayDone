package com.jsworld.android.daydone.domain.model

/**
 * 리포트용 지출 카테고리. 지출 입력에는 노출하지 않고,
 * 지출명 키워드 매칭으로 리포트 계산 시점에 파생된다.
 */
enum class ExpenseCategory(val label: String, val emoji: String) {
    PET("반려동물", "🐾"),
    OCCASION("경조사·선물", "🎁"),
    TRAVEL("여행·숙박", "✈️"),
    EDUCATION("교육", "📚"),
    SUBSCRIPTION("구독·통신", "📱"),
    HEALTH("의료·건강", "💊"),
    BEAUTY("뷰티·미용", "💇"),
    TRANSPORT("교통·차량", "🚕"),
    ALCOHOL("술·모임", "🍺"),
    GROCERY("장보기·마트", "🛒"),
    CAFE("카페·간식", "☕"),
    FOOD("식비", "🍚"),
    FASHION("쇼핑·패션", "👕"),
    LIVING("생활·잡화", "🧺"),
    CULTURE("문화·여가", "🎬"),
    ETC("기타", "💸")
}
