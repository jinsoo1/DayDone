package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.DailyBudgetSnapshot
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 오늘 권장 금액 계산의 **유일한 산술 코어** (§7).
 *
 * 순수 함수 — Flow 도, DB 도 모른다. 데이터를 이미 들고 있는 쪽(TodayViewModel)과
 * 직접 읽어와야 하는 쪽(ObserveDailyBudgetUseCase → 위젯·보류함)이 **둘 다 이걸 호출**한다.
 * 계산식을 바꿀 일이 있으면 여기만 바꾸면 된다.
 *
 *   오늘 권장 = (예산 + 추가수익 − 예정차감 − 오늘 이전 지출) ÷ 남은 일수
 *   내일 권장 = (위 값 − 오늘 지출) ÷ (남은 일수 − 1)
 */
class CalculateDailyBudgetUseCase @Inject constructor(
    private val calculateTodayDefenseLineUseCase: CalculateTodayDefenseLineUseCase
) {
    operator fun invoke(
        today: LocalDate,
        period: BudgetPeriod,
        monthlyBudget: Long,
        extraIncomeTotal: Long,
        scheduledDeductionTotal: Long,
        expenses: List<Expense>
    ): DailyBudgetSnapshot {
        // 미래 날짜에 미리 적어둔 지출은 아직 쓴 돈이 아니다 (오늘 탭과 같은 규칙)
        val pastSpent = expenses
            .filter { it.date.isBefore(today) }
            .sumOf { it.amount }

        val todayExpenses = expenses.filter { it.date == today }

        // 오늘 "쓴 돈"은 일반 지출만. 준비금(FUTURE_PREPARE)은 금고로 옮겨둔 돈이라
        // 생활비에서는 빠지지만 오늘 권장을 "넘긴" 게 아니다 — 권장 금액의 분자(budgetBeforeToday)
        // 에서 미리 빼서 오늘 권장 자체를 낮춘다. 안 그러면 앱이 권한 "금고에 준비하기"를
        // 누른 직후 카드가 "오늘 권장보다 더 썼어요"로 바뀐다. (무지출 판정·저녁 알림도 GENERAL만 본다)
        val preparedToday = todayExpenses
            .filter { it.type == ExpenseType.FUTURE_PREPARE }
            .sumOf { it.amount }

        val todaySpent = todayExpenses
            .filter { it.type == ExpenseType.GENERAL }
            .sumOf { it.amount }

        val budgetBeforeToday = monthlyBudget +
                extraIncomeTotal -
                scheduledDeductionTotal -
                pastSpent -
                preparedToday

        val remainingDays = ChronoUnit.DAYS.between(today, period.endDate).toInt() + 1

        val todayRecommended = calculateTodayDefenseLineUseCase(
            remainingPureBudget = budgetBeforeToday,
            today = today,
            budgetPeriod = period
        )

        val remainingPureBudget = budgetBeforeToday - todaySpent

        // 기간 마지막 날(또는 기간이 지난 날)에는 나눌 내일이 없다
        val tomorrowRecommended = if (remainingDays > 1) {
            remainingPureBudget / (remainingDays - 1)
        } else {
            null
        }

        return DailyBudgetSnapshot(
            period = period,
            remainingDays = remainingDays,
            budgetBeforeToday = budgetBeforeToday,
            remainingPureBudget = remainingPureBudget,
            todayRecommended = todayRecommended,
            todaySpent = todaySpent,
            todayLeft = todayRecommended - todaySpent,
            tomorrowRecommended = tomorrowRecommended
        )
    }
}
