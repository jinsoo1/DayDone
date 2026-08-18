package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.HeldPurchase
import com.jsworld.android.daydone.domain.model.HeldPurchaseStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HeldPurchaseRepository {

    fun observeAll(): Flow<List<HeldPurchase>>

    suspend fun hold(title: String, amount: Long, heldAt: LocalDate)

    suspend fun resolve(id: Long, status: HeldPurchaseStatus, resolvedAt: LocalDate)

    suspend fun delete(id: Long)
}
