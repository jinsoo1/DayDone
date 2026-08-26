package com.jsworld.android.daydone.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * 재부팅하면 예약이 사라진다. 다시 걸어준다.
 * RECEIVE_BOOT_COMPLETED 는 민감 권한이 아니다.
 */
class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // 리시버에서 오래 붙잡지 않도록 워커에 넘긴다
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RescheduleWorker>().build()
        )
    }
}
