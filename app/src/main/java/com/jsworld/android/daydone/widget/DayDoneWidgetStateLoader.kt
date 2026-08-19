package com.jsworld.android.daydone.widget

import android.content.Context
import com.jsworld.android.daydone.domain.usecase.ObserveDailyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveOnboardingDoneUseCase
import com.jsworld.android.daydone.presentation.util.toMoneyText
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * 위젯이 쓸 상태를 만든다.
 *
 * 위젯은 Hilt 주입 지점이 아니라 [EntryPoint] 로 UseCase 를 꺼낸다.
 * 계산은 앱과 **같은** [ObserveDailyBudgetUseCase] — 숫자가 오늘 탭과 어긋날 수 없다.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun observeDailyBudgetUseCase(): ObserveDailyBudgetUseCase
    fun observeOnboardingDoneUseCase(): ObserveOnboardingDoneUseCase
}

internal suspend fun loadWidgetState(context: Context): DayDoneWidgetState =
    runCatching { loadWidgetStateOrThrow(context) }
        .getOrElse { DayDoneWidgetState.Unavailable }

private suspend fun loadWidgetStateOrThrow(context: Context): DayDoneWidgetState {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetEntryPoint::class.java
    )

    if (!entryPoint.observeOnboardingDoneUseCase()().first()) {
        return DayDoneWidgetState.NeedsSetup
    }

    // 매번 오늘 날짜를 새로 읽는다 — 자정을 넘겨도 다음 갱신에 바로 맞춰진다
    val today = LocalDate.now()
    val budget = entryPoint.observeDailyBudgetUseCase()(today).first()

    val progress = if (budget.todayRecommended > 0L) {
        (budget.todaySpent.toFloat() / budget.todayRecommended.toFloat()).coerceIn(0f, 1f)
    } else {
        if (budget.todaySpent > 0L) 1f else 0f
    }

    // 초과 상태에서는 오늘의 실패 대신 내일의 기준을 보여준다 (§13)
    val showTomorrowAsMain = budget.isTodayOver && budget.tomorrowRecommended != null

    return DayDoneWidgetState.Ready(
        amount = if (showTomorrowAsMain) {
            budget.tomorrowRecommended ?: 0L
        } else {
            budget.todayLeft.coerceAtLeast(0L)
        },
        label = when {
            showTomorrowAsMain -> "오늘은 조금 넘겼어요 · 내일부터"
            budget.isTodayOver -> "오늘은 조금 넘겼어요"
            else -> "오늘 남은 금액"
        },
        isOver = budget.isTodayOver,
        progress = progress,
        remainingDays = budget.remainingDays,
        periodText = with(budget.period) {
            "${startDate.monthValue}월 ${startDate.dayOfMonth}일 ~ " +
                    "${endDate.monthValue}월 ${endDate.dayOfMonth}일"
        },
        tomorrowAmount = budget.tomorrowRecommended.takeIf { !showTomorrowAsMain },
        spentText = "오늘 권장 ${budget.todayRecommended.toMoneyText()} 중 " +
                "${budget.todaySpent.toMoneyText()} 씀"
    )
}
