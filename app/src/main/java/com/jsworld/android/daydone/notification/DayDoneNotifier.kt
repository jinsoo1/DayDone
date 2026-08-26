package com.jsworld.android.daydone.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.ui.view.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/** 알림을 눌렀을 때 열 화면. MainActivity 가 인텐트 부가값으로 받아 처리한다. */
enum class NotificationTarget {
    TODAY,          // 오늘 탭
    EXPENSE_INPUT,  // 오늘 탭 + 지출 입력 시트
    HELD_PURCHASES, // 소비 보류함
    REPORT,         // 지난 기간 결산 리포트
    VAULT           // 금고 탭
}

/**
 * 알림 표시 담당.
 *
 * 채널은 **하나**만 둔다 — 종류별로 쪼개면 시스템 설정 화면이 어지러워지고,
 * 끄고 싶은 사람은 앱 안 설정에서 개별로 끄면 된다.
 */
@Singleton
class DayDoneNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun show(
        id: Int,
        title: String,
        body: String,
        target: NotificationTarget
    ) {
        if (!canPost()) return

        ensureChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET, target.name)
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    /** 권한이 없거나 시스템에서 알림을 끈 상태면 조용히 넘어간다. */
    private fun canPost(): Boolean {
        // POST_NOTIFICATIONS 는 Android 13(API 33)부터. 그 이전엔 권한 개념이 없다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }
        )
    }

    companion object {
        const val CHANNEL_ID = "daydone_daily"
        const val EXTRA_TARGET = "daydone_notification_target"

        // 알림 id — 같은 종류는 덮어쓴다
        const val ID_MORNING = 1001
        const val ID_EVENING = 1002
        const val ID_HELD = 1003
    }
}
