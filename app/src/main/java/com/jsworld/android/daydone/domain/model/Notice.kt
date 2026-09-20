package com.jsworld.android.daydone.domain.model

/** 공지사항 한 건. (assets/notices_ko.json · notices_ja.json — 언어별 별도 관리) */
data class Notice(
    val id: String,
    val version: String,
    val date: String,
    val title: String,
    val content: String,
    val next: List<String>
)
