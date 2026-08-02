package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.BackupFileInfo

interface BackupRepository {

    /** 전체 데이터(지출·저축/고정비·금고·예산·설정)를 JSON 문자열로 내보낸다. */
    suspend fun exportToJson(): String

    /**
     * 공용 다운로드 폴더의 DayDone 하위 폴더에 바로 저장하고, 표시용 경로를 돌려준다.
     * 지원하지 않는 환경이면 예외를 던진다(화면은 파일 선택창으로 대체).
     */
    suspend fun exportToDownloads(): String

    /** 지출 내역(월별 구역)·고정 지출 시트 2개짜리 엑셀(xlsx)을 다운로드 폴더에 저장하고 경로를 돌려준다. */
    suspend fun exportExcelToDownloads(): String

    /** JSON 을 읽어 기존 데이터를 모두 대체한다. 형식이 잘못되면 예외를 던진다. */
    suspend fun importFromJson(json: String)

    /**
     * 다운로드/DayDone 폴더에서 이 앱이 만든 백업 파일 목록을 최신순으로 돌려준다.
     * 재설치·기기 변경 후에는 파일이 있어도 빈 목록일 수 있다(MediaStore 소유권 소실).
     */
    suspend fun listBackupFiles(): List<BackupFileInfo>

    /** 목록에서 고른 파일을 읽어 복원한다. */
    suspend fun importFromFile(uri: String)
}
