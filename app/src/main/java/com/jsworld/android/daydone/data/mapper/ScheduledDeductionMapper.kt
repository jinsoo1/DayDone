package com.jsworld.android.daydone.data.mapper

import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionEntity
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import java.time.YearMonth

fun ScheduledDeductionEntity.toDomain(): ScheduledDeduction {
    return ScheduledDeduction(
        id = id,
        title = title,
        amount = amount,
        type = ScheduledDeductionType.valueOf(type),
        withdrawalDay = withdrawalDay,
        startYearMonth = YearMonth.parse(startYearMonth),
        endYearMonth = endYearMonth?.let { YearMonth.parse(it) },
        memo = memo
    )
}