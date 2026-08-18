package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.HeldPurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeldPurchaseDao {

    @Insert
    suspend fun insert(entity: HeldPurchaseEntity)

    @Query("UPDATE held_purchases SET status = :status, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, resolvedAt: String?)

    @Query("SELECT * FROM held_purchases ORDER BY heldAt DESC, id DESC")
    fun observeAll(): Flow<List<HeldPurchaseEntity>>

    @Query("DELETE FROM held_purchases WHERE id = :id")
    suspend fun delete(id: Long)
}
