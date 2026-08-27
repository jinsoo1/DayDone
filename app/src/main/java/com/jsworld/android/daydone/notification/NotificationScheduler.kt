package com.jsworld.android.daydone.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jsworld.android.daydone.domain.model.NotificationSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 알림 예약.
 *
 * ⚠️ 정확 알람(SCHEDULE_EXACT_ALARM)은 쓰지 않는다 — 민감 권한이다.
 * 다음 발송 시각까지 지연을 준 OneTimeWork 를 걸고, 워커가 끝나면서 다음 날을 다시 건다.
 * (PeriodicWork 는 최소 주기·정렬 문제로 "매일 8시"에 맞추기 어렵다)
 * Doze 로 몇 분 늦을 수 있는 건 감수한다.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 설정에 맞춰 예약을 다시 건다. 꺼진 알림은 취소한다.
     *
     * ⚠️ [policy] 를 반드시 구분해서 넘긴다.
     * - 설정을 바꿀 때는 [ExistingWorkPolicy.REPLACE] — 새 시각을 적용해야 하니까.
     * - 앱 시작·부팅처럼 **그냥 복구하는** 경로는 [ExistingWorkPolicy.KEEP].
     *   REPLACE 로 복구하면 아직 발송되지 않고 대기 중인 작업(밤새 Doze 로 밀린 아침 알림)이
     *   취소되고 다음 날로 다시 잡혀서, 아침에 앱을 열어보는 사람은 그 알림을 영영 못 받는다.
     *   KEEP 은 대기 중인 작업은 그대로 두고, 끝났거나 없을 때만 새로 건다.
     */
    fun reschedule(
        settings: NotificationSettings,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        val workManager = WorkManager.getInstance(context)

        if (settings.anyMorningEnabled) {
            workManager.enqueueUniqueWork(
                WORK_MORNING,
                policy,
                OneTimeWorkRequestBuilder<MorningNotificationWorker>()
                    .setInitialDelay(delayUntil(settings.morningHour))
                    .build()
            )
        } else {
            workManager.cancelUniqueWork(WORK_MORNING)
        }

        if (settings.anyEveningEnabled) {
            workManager.enqueueUniqueWork(
                WORK_EVENING,
                policy,
                OneTimeWorkRequestBuilder<EveningNotificationWorker>()
                    .setInitialDelay(delayUntil(settings.eveningHour))
                    .build()
            )
        } else {
            workManager.cancelUniqueWork(WORK_EVENING)
        }
    }

    /** 워커가 발송을 마친 뒤 다음 날을 다시 건다. */
    fun scheduleNext(work: String, hour: Int, workerIsMorning: Boolean) {
        val request = if (workerIsMorning) {
            OneTimeWorkRequestBuilder<MorningNotificationWorker>()
        } else {
            OneTimeWorkRequestBuilder<EveningNotificationWorker>()
        }.setInitialDelay(delayUntil(hour)).build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(work, ExistingWorkPolicy.REPLACE, request)
    }

    /** 오늘 그 시각이 지났으면 내일 같은 시각까지. */
    private fun delayUntil(hour: Int): Duration {
        val now = LocalDateTime.now()
        var target = now.withHour(hour.coerceIn(0, 23)).withMinute(0).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)

        val zone = ZoneId.systemDefault()
        return Duration.between(now.atZone(zone), target.atZone(zone))
    }

    companion object {
        const val WORK_MORNING = "daydone_morning_notification"
        const val WORK_EVENING = "daydone_evening_notification"
    }
}
