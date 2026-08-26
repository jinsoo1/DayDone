package com.jsworld.android.daydone.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.domain.usecase.BuildEveningNotificationUseCase
import com.jsworld.android.daydone.domain.usecase.BuildHeldPurchaseNotificationUseCase
import com.jsworld.android.daydone.domain.usecase.BuildMorningNotificationUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveNotificationSettingsUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateNotificationSettingsUseCase
import com.jsworld.android.daydone.notification.DayDoneNotifier
import com.jsworld.android.daydone.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    observeNotificationSettingsUseCase: ObserveNotificationSettingsUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val scheduler: NotificationScheduler,
    private val buildMorningNotificationUseCase: BuildMorningNotificationUseCase,
    private val buildEveningNotificationUseCase: BuildEveningNotificationUseCase,
    private val buildHeldPurchaseNotificationUseCase: BuildHeldPurchaseNotificationUseCase,
    private val notifier: DayDoneNotifier
) : ViewModel() {

    private val _settings = MutableStateFlow(NotificationSettings())
    val settings: StateFlow<NotificationSettings> = _settings.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    init {
        observeNotificationSettingsUseCase()
            .onEach { _settings.value = it }
            .launchIn(viewModelScope)
    }

    fun onMorningChange(enabled: Boolean) = save { it.copy(morningEnabled = enabled) }
    fun onEveningChange(enabled: Boolean) = save { it.copy(eveningEnabled = enabled) }
    fun onUpcomingDeductionChange(enabled: Boolean) =
        save { it.copy(upcomingDeductionEnabled = enabled) }
    fun onHeldChange(enabled: Boolean) = save { it.copy(heldPurchaseEnabled = enabled) }
    fun onReportChange(enabled: Boolean) = save { it.copy(periodReportEnabled = enabled) }
    fun onBigSpendChange(enabled: Boolean) = save { it.copy(bigSpendMonthEnabled = enabled) }
    fun onMorningHourChange(hour: Int) = save { it.copy(morningHour = hour) }
    fun onEveningHourChange(hour: Int) = save { it.copy(eveningHour = hour) }

    /** 권한을 거부당하면 켠 스위치를 되돌린다. */
    fun onPermissionDenied() = save {
        it.copy(
            morningEnabled = false,
            eveningEnabled = false,
            upcomingDeductionEnabled = false,
            heldPurchaseEnabled = false,
            periodReportEnabled = false,
            bigSpendMonthEnabled = false
        )
    }

    /**
     * 디버그 빌드 전용 — 시각을 기다리지 않고 지금 보내본다.
     * 실제 발송과 **같은 UseCase** 를 쓰므로 조건(오늘 지출 0건, 챌린지 성공 등)도 그대로 적용된다.
     * 보낼 게 없으면 그 사실을 알려준다.
     */
    fun onSendTestNotifications() {
        viewModelScope.launch {
            val settings = _settings.value
            val today = java.time.LocalDate.now()
            var sent = 0

            buildMorningNotificationUseCase(today, settings)?.let {
                notifier.show(DayDoneNotifier.ID_MORNING, it.title, it.body, it.target)
                sent++
            }
            buildEveningNotificationUseCase(today, settings)?.let {
                notifier.show(DayDoneNotifier.ID_EVENING, it.title, it.body, it.target)
                sent++
            }
            buildHeldPurchaseNotificationUseCase(today)?.let {
                notifier.show(DayDoneNotifier.ID_HELD, it.title, it.body, it.target)
                sent++
            }

            _testResult.value = when {
                sent > 0 -> "${sent}건 보냈어요"
                !settings.anyEnabled -> "켜진 알림이 없어요"
                else -> "지금은 보낼 알림이 없어요 (오늘 지출이 이미 있거나, 조건에 안 맞아요)"
            }
        }
    }

    fun onTestResultShown() {
        _testResult.value = null
    }

    private fun save(transform: (NotificationSettings) -> NotificationSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated

        viewModelScope.launch {
            updateNotificationSettingsUseCase(updated)
            // 설정이 바뀔 때마다 예약을 다시 건다 (끈 알림은 취소된다)
            scheduler.reschedule(updated)
        }
    }
}
