package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.HeldPurchaseDao
import com.jsworld.android.daydone.data.local.entity.HeldPurchaseEntity
import com.jsworld.android.daydone.domain.model.HeldPurchase
import com.jsworld.android.daydone.domain.model.HeldPurchaseStatus
import com.jsworld.android.daydone.domain.repository.HeldPurchaseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HeldPurchaseRepositoryImpl @Inject constructor(
    private val dao: HeldPurchaseDao
) : HeldPurchaseRepository {

    override fun observeAll(): Flow<List<HeldPurchase>> {
        return dao.observeAll().map { entities ->
            entities.map { entity ->
                HeldPurchase(
                    id = entity.id,
                    title = entity.title,
                    amount = entity.amount,
                    heldAt = LocalDate.parse(entity.heldAt),
                    status = runCatching { HeldPurchaseStatus.valueOf(entity.status) }
                        .getOrDefault(HeldPurchaseStatus.HELD),
                    resolvedAt = entity.resolvedAt?.let {
                        runCatching { LocalDate.parse(it) }.getOrNull()
                    }
                )
            }
        }
    }

    override suspend fun hold(title: String, amount: Long, heldAt: LocalDate) {
        dao.insert(
            HeldPurchaseEntity(
                title = title,
                amount = amount,
                heldAt = heldAt.toString(),
                status = HeldPurchaseStatus.HELD.name,
                resolvedAt = null
            )
        )
    }

    override suspend fun resolve(id: Long, status: HeldPurchaseStatus, resolvedAt: LocalDate) {
        dao.updateStatus(
            id = id,
            status = status.name,
            resolvedAt = resolvedAt.toString()
        )
    }

    override suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
