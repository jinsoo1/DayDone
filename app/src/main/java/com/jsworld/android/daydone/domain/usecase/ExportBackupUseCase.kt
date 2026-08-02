package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BackupRepository
import jakarta.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): String = repository.exportToJson()
}
