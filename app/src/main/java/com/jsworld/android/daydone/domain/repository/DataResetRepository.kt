package com.jsworld.android.daydone.domain.repository

interface DataResetRepository {

    /** 모든 로컬 데이터(DB 전 테이블 + 설정)를 삭제한다. */
    suspend fun resetAll()
}
