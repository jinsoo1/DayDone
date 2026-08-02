package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BackupRepository
import jakarta.inject.Inject

/** 백업 목록에서 고른 파일을 읽어 전체 데이터를 대체한다. */
class ImportBackupFromFileUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(uri: String) = repository.importFromFile(uri)
}
