package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_expenses")
data class QuickExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Long,
    val sortOrder: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)