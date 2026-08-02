package com.jsworld.android.daydone.data.repository

import android.content.Context
import com.jsworld.android.daydone.domain.model.Notice
import com.jsworld.android.daydone.domain.repository.NoticeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NoticeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NoticeRepository {

    override suspend fun getNotices(): List<Notice> = withContext(Dispatchers.IO) {
        val raw = context.assets.open(FILE_NAME)
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

    companion object {
        private const val FILE_NAME = "ddaydone_notices.json"
    }
}
