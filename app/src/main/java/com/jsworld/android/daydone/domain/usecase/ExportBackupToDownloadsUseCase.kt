package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BackupRepository
import jakarta.inject.Inject

/** 다운로드/DayDone 폴더에 바로 저장하고 표시용 경로를 돌려준다. */
class ExportBackupToDownloadsUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): String = repository.exportToDownloads()
}
