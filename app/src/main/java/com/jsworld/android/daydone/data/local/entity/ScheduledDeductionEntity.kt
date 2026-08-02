package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_deductions")
data class ScheduledDeductionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Long,
    val type: String,
    val withdrawalDay: Int,
    val startYearMonth: String,
    val endYearMonth: String?,
    val memo: String?,
    val createdAt: Long = System.currentTimeMillis()
)