package com.jsworld.android.daydone.data.mapper

import com.jsworld.android.daydone.data.local.entity.FutureExpenseEntity
import com.jsworld.android.daydone.domain.model.FutureExpense
import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import java.time.YearMonth

fun FutureExpenseEntity.toDomain(): FutureExpense {
    return FutureExpense(
        id = id,
        title = title,
        category = FutureExpenseCategory.valueOf(category),
        totalAmount = totalAmount,
        targetYearMonth = YearMonth.parse(targetYearMonth),
        prepareStartYearMonth = YearMonth.parse(prepareStartYearMonth),
        repeat = FutureExpenseRepeat.valueOf(repeatRule),
        memo = memo,
        lastPaidYearMonth = lastPaidYearMonth?.let { YearMonth.parse(it) }
    )
}
