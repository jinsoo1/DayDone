package com.jsworld.android.daydone.domain.model

/** 다운로드 폴더에서 찾은 백업 파일 하나. [uri] 는 content:// 문자열. */
data class BackupFileInfo(
    val name: String,
    val uri: String,
    val modifiedAtMillis: Long,
    val sizeBytes: Long
)
