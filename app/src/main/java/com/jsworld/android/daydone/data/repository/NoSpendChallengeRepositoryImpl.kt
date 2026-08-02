package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.datastore.NoSpendChallengeDataSource
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class NoSpendChallengeRepositoryImpl @Inject constructor(
    private val dataSource: NoSpendChallengeDataSource
) : NoSpendChallengeRepository {

    override val settingsFlow: Flow<NoSpendChallengeSettings>
        get() = dataSource.settingsFlow

    override suspend fun update(settings: NoSpendChallengeSettings) {
        dataSource.update(settings)
    }
}
