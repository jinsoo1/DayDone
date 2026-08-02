package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BackupRepository
import jakarta.inject.Inject

/**
 * 지출 내역(월별 구역)과 고정 지출, 시트 2개짜리 엑셀 파일을
 * 다운로드/DayDone/엑셀 폴더에 저장하고 경로를 돌려준다.
 */
class ExportExcelUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): String = repository.exportExcelToDownloads()
}
