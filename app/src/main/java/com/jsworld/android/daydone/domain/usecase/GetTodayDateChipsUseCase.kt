package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.presentation.today.model.TodayDateChipUiModel
import java.time.LocalDate

class GetTodayDateChipsUseCase {

    companion object {
        /**
         * 오늘 기준 앞뒤 며칠까지 칩으로 보여줄지 (§9).
         * 칩은 예산 기간 경계를 넘어가므로, 내역을 읽어오는 쪽도 이 범위를 같이 써야
         * 지난/다음 기간의 칩이 "내역 없음"으로 보이지 않는다.
         */
        const val DAYS_BEFORE = 3
        const val DAYS_AFTER = 3
    }

    operator fun invoke(
        today: LocalDate,
        selectedDate: LocalDate,
        expenseDates: Set<LocalDate>,
        scheduledDeductionDates: Set<LocalDate>
    ): List<TodayDateChipUiModel> {
        return (-DAYS_BEFORE..DAYS_AFTER).map { offset ->
            val date = today.plusDays(offset.toLong())

            TodayDateChipUiModel(
                date = date,
                dayText = date.dayOfMonth.toString(),
                weekText = date.toKoreanWeekText(),
                isToday = date == today,
                isSelected = date == selectedDate,
                hasExpense = expenseDates.contains(date),
                hasScheduledDeduction = scheduledDeductionDates.contains(date)
            )
        }
    }

    private fun LocalDate.toKoreanWeekText(): String {
        return when (dayOfWeek.value) {
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            else -> "일"
        }
    }
}