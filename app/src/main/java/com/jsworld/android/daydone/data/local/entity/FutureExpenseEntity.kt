package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "future_expenses")
data class FutureExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val category: String,
    val totalAmount: Long,
    val targetYearMonth: String,        // "YYYY-MM"
    val prepareStartYearMonth: String,  // "YYYY-MM"
    val repeatRule: String,             // ONCE / YEARLY
    val memo: String?,
    val lastPaidYearMonth: String?,     // "YYYY-MM" | null
    val createdAt: Long = System.currentTimeMillis()
)
