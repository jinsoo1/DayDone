package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Long,
    val date: String,
    val type: String,
    val futureExpenseId: Long? = null,
    val memo: String? = null,
    val isEssential: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)