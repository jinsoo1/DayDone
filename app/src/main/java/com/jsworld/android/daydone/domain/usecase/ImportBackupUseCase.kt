package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BackupRepository
import jakarta.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(json: String) = repository.importFromJson(json)
}
