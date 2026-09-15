package com.jsworld.android.daydone.widget

import android.content.Context
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.DailyBudgetSnapshot
import com.jsworld.android.daydone.domain.usecase.ObserveDailyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveOnboardingDoneUseCase
import com.jsworld.android.daydone.presentation.util.toMoneyText
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * 위젯은 Hilt 주입 지점이 아니라 [EntryPoint] 로 UseCase 를 꺼낸다.
 * 계산은 앱과 **같은** [ObserveDailyBudgetUseCase] — 숫자가 오늘 탭과 어긋날 수 없다.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun observeDailyBudgetUseCase(): ObserveDailyBudgetUseCase
    fun observeOnboardingDoneUseCase(): ObserveOnboardingDoneUseCase
}

/**
 * 위젯이 그릴 상태를 **흐름으로** 내보낸다.
 *
 * ⚠️ 값 하나를 읽어 넘기면 안 된다. Glance 는 세션이 살아 있는 동안
 * provideGlance 를 다시 부르지 않고 화면만 재구성하므로, 처음 읽은 값이
 * 계속 다시 그려진다 — 지출을 넣어도 위젯이 한 박자 늦는 원인이었다.
 * 화면 안에서 이 흐름을 구독해야 데이터가 바뀔 때 곧바로 따라온다.
 */
internal fun widgetStateFlow(context: Context): Flow<DayDoneWidgetState> = flow {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetEntryPoint::class.java
    )

    val source = if (!entryPoint.observeOnboardingDoneUseCase()().first()) {
        flowOf(DayDoneWidgetState.NeedsSetup)
    } else {
        // 세션이 새로 시작될 때마다 오늘 날짜를 다시 읽는다
        entryPoint.observeDailyBudgetUseCase()(LocalDate.now())
            .map { it.toWidgetState(context) }
    }

    emitAll(source)
}.catch { emit(DayDoneWidgetState.Unavailable) }

/** [context] 는 라벨 문구를 만들기 위한 것 — 위젯은 Composable 밖에서 상태를 만든다. */
private fun DailyBudgetSnapshot.toWidgetState(context: Context): DayDoneWidgetState {
    val progress = when {
        todayRecommended > 0L ->
            (todaySpent.toFloat() / todayRecommended.toFloat()).coerceIn(0f, 1f)

        todaySpent > 0L -> 1f
        else -> 0f
    }

    // 초과한 날에는 오늘의 실패 대신 내일의 기준을 보여준다 (§13)
    val showTomorrowAsMain = isTodayOver && tomorrowRecommended != null

    return DayDoneWidgetState.Ready(
        amount = if (showTomorrowAsMain) {
            tomorrowRecommended ?: 0L
        } else {
            todayLeft.coerceAtLeast(0L)
        },
        label = when {
            showTomorrowAsMain -> context.getString(R.string.widget_oneuleun_jogeum_neomgyeosseoyo_naeilbuteo)
            isTodayOver -> context.getString(R.string.widget_oneuleun_jogeum_neomgyeosseoyo)
            else -> context.getString(R.string.widget_oneul_nameun_geumaeg)
        },
        isOver = isTodayOver,
        progress = progress,
        remainingDays = remainingDays,
        periodText = context.getString(R.string.widget_weol_il_weol_il, period.startDate.monthValue, period.startDate.dayOfMonth, period.endDate.monthValue, period.endDate.dayOfMonth),
        tomorrowAmount = tomorrowRecommended.takeIf { !showTomorrowAsMain },
        spentText = context.getString(R.string.widget_oneul_gweonjang_jung_sseum, todayRecommended.toMoneyText(), todaySpent.toMoneyText())
    )
}
