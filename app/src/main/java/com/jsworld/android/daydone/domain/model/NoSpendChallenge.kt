package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

/** 무지출 성공 기준. */
enum class NoSpendMode {
    FULL,              // 완전 무지출: 그날 일반 지출이 하나도 없어야 성공
    ESSENTIAL_ALLOWED, // 필수 지출 허용: '필수 지출'로 표시된 것 외에 없으면 성공
    CAP                // 금액 상한: 그날 일반 지출 합(필수 제외)이 상한 이하면 성공
}

/**
 * 무지출 챌린지 설정. 예산 기간과 무관하게, [startDate]부터 [targetDays]일 동안 진행한다.
 * [startDate]가 null이면 아직 시작 전.
 */
data class NoSpendChallengeSettings(
    val enabled: Boolean,
    val mode: NoSpendMode,
    val capAmount: Long,
    val targetDays: Int,
    val startDate: LocalDate?
)

/** 무지출 챌린지 진행 상황 (파생). */
data class NoSpendProgress(
    val successDays: Int,        // 오늘 이전 확정 성공 일수
    val isTodayOnTrack: Boolean, // 오늘 현재까지 조건 유지 중인지
    val streak: Int,             // 오늘 포함 연속 성공 일수
    val dayIndex: Int,           // 오늘이 챌린지 며칠째인지 (1-based), 기간 밖이면 0
    val totalDays: Int,          // 도전 일수
    val isFinished: Boolean      // 챌린지 기간이 끝났는지
)
