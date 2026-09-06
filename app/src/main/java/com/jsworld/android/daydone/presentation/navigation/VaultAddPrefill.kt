package com.jsworld.android.daydone.presentation.navigation

/** 살까 말까 "금고에 준비하기" → 금고 탭 준비 항목 추가 시트 프리필. */
data class VaultAddPrefill(
    val title: String,
    val amount: Long,
    /**
     * 목표월을 이번 기간 기준 몇 달 뒤로 잡을지.
     * 살까 말까(모아서 사기)는 기본 3개월 뒤, 기간 막바지 여유분은 0 —
     * 0이어야 이번 달 준비 제안액이 전액이 되어 지금 바로 옮길 수 있다.
     */
    val monthsAhead: Int = 3
)
