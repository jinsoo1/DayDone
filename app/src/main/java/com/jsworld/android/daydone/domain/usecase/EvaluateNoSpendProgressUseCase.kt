package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.model.NoSpendProgress
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 무지출 챌린지 진행을 파생한다. 예산 기간과 무관하게 [시작일, 시작일+도전일수-1] 창을 본다.
 * - 일반 지출(GENERAL)만 본다. 준비금(FUTURE_PREPARE)은 저축 성격이라 무지출을 깨지 않는다.
 * - FULL: 그날 지출 없음 / ESSENTIAL_ALLOWED: 필수 표시 외 없음 / CAP: 필수 제외 합계 ≤ 상한.
 * - 성공 일수는 "지난 날"만 확정 집계, 오늘은 진행 중(isTodayOnTrack)으로 따로 표시.
 */
class EvaluateNoSpendProgressUseCase @Inject constructor() {

    operator fun invoke(
        expenses: List<Expense>,
        settings: NoSpendChallengeSettings,
        today: LocalDate
    ): NoSpendProgress {
        val start = settings.startDate
        val totalDays = settings.targetDays

        if (!settings.enabled || start == null || totalDays <= 0) {
            return NoSpendProgress(
                successDays = 0,
                isTodayOnTrack = false,
                streak = 0,
                dayIndex = 0,
                totalDays = totalDays,
                isFinished = false
            )
        }

        val startDate: LocalDate = start
        val end = startDate.plusDays((totalDays - 1).toLong())

        val generalByDate = expenses
            .filter { it.type == ExpenseType.GENERAL }
            .groupBy { it.date }

        fun isSuccess(date: LocalDate): Boolean {
            return isSuccessDay(generalByDate[date].orEmpty(), settings)
        }

        // 지난 날 확정 성공 일수 ([start, min(today-1, end)])
        var successDays = 0
        val lastConfirmed = minOf(today.minusDays(1), end)
        var cursor = startDate
        while (!cursor.isAfter(lastConfirmed)) {
            if (isSuccess(cursor)) successDays++
            cursor = cursor.plusDays(1)
        }

        val todayInWindow = !today.isBefore(startDate) && !today.isAfter(end)
        val isTodayOnTrack = todayInWindow && isSuccess(today)
        val isFinished = today.isAfter(end)
        val dayIndex = if (todayInWindow) {
            ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        } else {
            0
        }

        // 연속: 오늘(진행 중이면 포함) 또는 어제부터 거꾸로
        var streak = 0
        if (todayInWindow) {
            if (isTodayOnTrack) streak = 1
            var back = today.minusDays(1)
            while (!back.isBefore(startDate) && isSuccess(back)) {
                streak++
                back = back.minusDays(1)
            }
        }

        return NoSpendProgress(
            successDays = successDays,
            isTodayOnTrack = isTodayOnTrack,
            streak = streak,
            dayIndex = dayIndex,
            totalDays = totalDays,
            isFinished = isFinished
        )
    }

    /** 하루치 지출 목록으로 그날의 무지출 성공 여부를 판정한다. (일반 지출만 넘길 것) */
    fun isSuccessDay(
        dayExpenses: List<Expense>,
        settings: NoSpendChallengeSettings
    ): Boolean {
        val general = dayExpenses.filter { it.type == ExpenseType.GENERAL }
        return when (settings.mode) {
            NoSpendMode.FULL ->
                general.isEmpty()

            NoSpendMode.ESSENTIAL_ALLOWED ->
                general.none { !it.isEssential }

            NoSpendMode.CAP ->
                general.filter { !it.isEssential }.sumOf { it.amount } <= settings.capAmount
        }
    }
}
