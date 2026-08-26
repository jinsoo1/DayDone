package com.jsworld.android.daydone.notification

import android.content.Context
import com.jsworld.android.daydone.domain.usecase.BuildEveningNotificationUseCase
import com.jsworld.android.daydone.domain.usecase.BuildHeldPurchaseNotificationUseCase
import com.jsworld.android.daydone.domain.usecase.BuildMorningNotificationUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveNotificationSettingsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 워커가 의존성을 꺼내는 통로.
 *
 * `@HiltWorker` 를 쓰면 hilt-work 의존성 + Application 의 Configuration.Provider +
 * 매니페스트에서 WorkManagerInitializer 제거까지 따라온다. 위젯에서 이미 쓰는
 * EntryPoint 방식이 부품이 적고 R8 위험도 낮아 같은 방식으로 통일한다.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NotificationEntryPoint {
    fun observeNotificationSettingsUseCase(): ObserveNotificationSettingsUseCase
    fun buildMorningNotificationUseCase(): BuildMorningNotificationUseCase
    fun buildEveningNotificationUseCase(): BuildEveningNotificationUseCase
    fun buildHeldPurchaseNotificationUseCase(): BuildHeldPurchaseNotificationUseCase
    fun notifier(): DayDoneNotifier
    fun scheduler(): NotificationScheduler
}

internal fun notificationEntryPoint(context: Context): NotificationEntryPoint =
    EntryPointAccessors.fromApplication(
        context.applicationContext,
        NotificationEntryPoint::class.java
    )
