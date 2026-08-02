package com.jsworld.android.daydone.domain.model

/** 공지사항 한 건. (assets/ddaydone_notices.json) */
data class Notice(
    val id: String,
    val version: String,
    val date: String,
    val title: String,
    val content: String,
    val next: List<String>
)
