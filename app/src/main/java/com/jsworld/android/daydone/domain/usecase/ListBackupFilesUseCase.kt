package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BackupFileInfo
import com.jsworld.android.daydone.domain.repository.BackupRepository
import jakarta.inject.Inject

/** 다운로드/DayDone 폴더의 백업 파일 목록 (최신순). 재설치 후에는 비어 있을 수 있다. */
class ListBackupFilesUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): List<BackupFileInfo> = repository.listBackupFiles()
}
