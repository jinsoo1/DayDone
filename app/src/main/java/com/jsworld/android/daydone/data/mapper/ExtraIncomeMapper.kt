package com.jsworld.android.daydone.data.mapper

import com.jsworld.android.daydone.data.local.entity.ExtraIncomeEntity
import com.jsworld.android.daydone.domain.model.ExtraIncome
import java.time.LocalDate


fun ExtraIncomeEntity.toDomain(): ExtraIncome {
    return ExtraIncome(
        id = id,
        title = title,
        amount = amount,
        date = LocalDate.parse(date),
        memo = memo
    )
}