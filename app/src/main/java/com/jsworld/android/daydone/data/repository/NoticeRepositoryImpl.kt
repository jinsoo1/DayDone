package com.jsworld.android.daydone.data.repository

import android.content.Context
import com.jsworld.android.daydone.domain.model.Notice
import com.jsworld.android.daydone.domain.repository.NoticeRepository
import com.jsworld.android.daydone.presentation.util.AppLocale
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NoticeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NoticeRepository {

    override suspend fun getNotices(): List<Notice> = withContext(Dispatchers.IO) {
        val raw = context.assets.open(fileName())
            .bufferedReader()
            .use { it.readText() }

        val array = JSONObject(raw).getJSONArray("notices")

        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val nextArray = obj.optJSONArray("next")
                val next = if (nextArray != null) {
                    (0 until nextArray.length()).map { nextArray.getString(it) }
                } else {
                    emptyList()
                }

                add(
                    Notice(
                        id = obj.getString("id"),
                        version = obj.optString("version"),
                        date = obj.getString("date"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        next = next
                    )
                )
            }
        }.sortedByDescending { it.date }
    }

    /**
     * 공지는 언어별로 **따로 관리**한다 — 번역이 아니라 각 나라 유저에게 할 말이 다르다.
     * 일본어 공지는 1.5.0(일본어 첫 출시)이 첫 공지고, 한국어 공지의 과거 이력은 담지 않는다.
     */
    private fun fileName(): String = when (AppLocale.current.language) {
        "ja" -> "notices_ja.json"
        else -> "notices_ko.json"
    }
}
