package com.jsworld.android.daydone.widget

/**
 * 위젯이 그리는 데 필요한 값만 담은 화면 모델.
 *
 * ⚠️ 초과 상태여도 **빨간 숫자를 쓰지 않는다**(docs/v1.3-design.md §2).
 * 홈 화면에 하루 종일 잔소리를 띄워두지 않기 위해, 초과일 때는 오늘의 실패 대신
 * **내일 권장 금액**을 큰 숫자로 보여준다.
 */
sealed interface DayDoneWidgetState {

    /** 첫 구독 전 아주 잠깐. */
    data object Loading : DayDoneWidgetState

    /** 온보딩 전 — 예산이 없어 계산할 수 없다. */
    data object NeedsSetup : DayDoneWidgetState

    /**
     * 값을 불러오지 못했을 때.
     * 여기서 예외를 던지면 런처가 "위젯 로드 중 문제 발생" 박스를 띄운다.
     * 그것보다 조용히 안내하고 다음 갱신을 기다리는 편이 낫다.
     */
    data object Unavailable : DayDoneWidgetState

    data class Ready(
        val amount: Long,          // 큰 숫자로 보여줄 금액
        val label: String,         // 큰 숫자 위 라벨
        val isOver: Boolean,       // 오늘 권장을 넘긴 상태인지
        val progress: Float,       // 오늘 권장 대비 사용 비율 0f~1f
        val remainingDays: Int,
        val periodText: String,    // "8월 1일 ~ 8월 31일"
        val tomorrowAmount: Long?, // 4×2 보조 표시 (마지막 날이면 null)
        val spentText: String      // "오늘 권장 32,000원 중 7,400원 씀"
    ) : DayDoneWidgetState
}
