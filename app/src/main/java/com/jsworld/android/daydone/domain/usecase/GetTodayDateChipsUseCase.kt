package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.presentation.today.model.TodayDateChipUiModel
import java.time.LocalDate

class GetTodayDateChipsUseCase {

    operator fun invoke(
        today: LocalDate,
        selectedDate: LocalDate,
        expenseDates: Set<LocalDate>,
        scheduledDeductionDates: Set<LocalDate>
    ): List<TodayDateChipUiModel> {
        return (-3..3).map { offset ->
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