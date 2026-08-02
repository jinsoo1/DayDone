package com.jsworld.android.daydone.domain.model

data class QuickExpense(
    val id: Long,
    val title: String,
    val amount: Long,
    val sortOrder: Int,
    val isActive: Boolean
)