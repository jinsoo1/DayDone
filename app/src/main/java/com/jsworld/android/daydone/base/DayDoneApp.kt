package com.jsworld.android.daydone.base

import android.app.Application
import android.util.Log
import com.jsworld.android.daydone.domain.usecase.ObserveDailyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveNotificationSettingsUseCase
import com.jsworld.android.daydone.notification.NotificationScheduler
import com.jsworld.android.daydone.widget.refreshDayDoneWidget
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltAndroidApp
class DayDoneApp : Application() {

    @Inject
    lateinit var observeDailyBudgetUseCase: ObserveDailyBudgetUseCase

    @Inject
    lateinit var observeNotificationSettingsUseCase: ObserveNotificationSettingsUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        observeBudgetForWidget()
        restoreNotificationSchedule()
    }

    /** 켜둔 알림의 예약이 유실됐을 수 있으니 앱이 뜰 때 한 번 다시 건다. */
    private fun restoreNotificationSchedule() {
        appScope.launch {
            val settings = observeNotificationSettingsUseCase().first()
            if (settings.anyEnabled) {
                notificationScheduler.reschedule(settings)
            }
        }
    }

    /**
     * 지출·수익·예산·예정 차감이 바뀌면 홈 위젯을 갱신한다.
     *
     * UseCase 마다 갱신 호출을 흩뿌리는 대신 **계산 결과 흐름 한 곳**을 구독한다.
     * 어떤 경로로 데이터가 바뀌든 결과가 달라지면 위젯이 따라온다.
     * (자정 넘김은 위젯의 updatePeriodMillis 가 받쳐준다)
     */
    private fun observeBudgetForWidget() {
        observeDailyBudgetUseCase(LocalDate.now())
            .distinctUntilChanged()
            .onEach { refreshDayDoneWidget(this) }
            .catch { Log.w("DayDoneWidget", "예산 흐름 오류", it) }
            .launchIn(appScope)
    }
}
