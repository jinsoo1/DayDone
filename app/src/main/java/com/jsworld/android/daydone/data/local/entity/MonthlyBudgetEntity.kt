package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
    @PrimaryKey
    val anchorMonth: String, // "2026-07" (기간의 기준 달)
    val income: Long
)
