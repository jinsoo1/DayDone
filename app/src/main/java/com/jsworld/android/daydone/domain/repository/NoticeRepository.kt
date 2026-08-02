package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.Notice

interface NoticeRepository {

    /** 최신순으로 정렬된 공지 목록. */
    suspend fun getNotices(): List<Notice>
}
