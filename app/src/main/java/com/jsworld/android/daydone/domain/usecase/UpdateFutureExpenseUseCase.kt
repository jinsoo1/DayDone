package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import jakarta.inject.Inject
import java.time.YearMonth

class UpdateFutureExpenseUseCase @Inject constructor(
    private val repository: FutureExpenseRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String,
        category: FutureExpenseCategory,
        totalAmount: Long,
        targetYearMonth: YearMonth,
        prepareStartYearMonth: YearMonth,
        repeat: FutureExpenseRepeat,
        memo: String?
    ) {
        repository.update(
            id = id,
            title = title,
            category = category,
            totalAmount = totalAmount,
            targetYearMonth = targetYearMonth,
            prepareStartYearMonth = prepareStartYearMonth,
            repeat = repeat,
            memo = memo
        )
    }
}
