package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.Notice
import com.jsworld.android.daydone.domain.repository.NoticeRepository
import jakarta.inject.Inject

class GetNoticesUseCase @Inject constructor(
    private val repository: NoticeRepository
) {
    suspend operator fun invoke(): List<Notice> {
        return repository.getNotices()
    }
}
