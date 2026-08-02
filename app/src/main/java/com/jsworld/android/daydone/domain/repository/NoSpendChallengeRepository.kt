package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import kotlinx.coroutines.flow.Flow

interface NoSpendChallengeRepository {

    val settingsFlow: Flow<NoSpendChallengeSettings>

    suspend fun update(settings: NoSpendChallengeSettings)
}
