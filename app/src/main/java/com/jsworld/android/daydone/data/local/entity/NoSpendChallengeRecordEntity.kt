package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 끝난 무지출 챌린지 기록. startDate(ISO)가 키 — 같은 도전은 한 번만 저장된다. */
@Entity(tableName = "no_spend_records")
data class NoSpendChallengeRecordEntity(
    @PrimaryKey val startDate: String,
    val targetDays: Int,
    val successDays: Int,
    val mode: String,
    val capAmount: Long,
    val createdAt: Long = System.currentTimeMillis()
)
