package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extra_incomes")
data class ExtraIncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Long,
    val date: String,
    val memo: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)