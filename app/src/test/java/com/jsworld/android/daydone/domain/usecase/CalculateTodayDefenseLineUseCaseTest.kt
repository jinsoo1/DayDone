package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * 오늘 권장 금액 = 남은 순수 생활비 ÷ 남은 일수 (§7).
 */
class CalculateTodayDefenseLineUseCaseTest {

    private val useCase = CalculateTodayDefenseLineUseCase()

    private val period = BudgetPeriod(
        startDate = LocalDate.of(2026, 7, 1),
        endDate = LocalDate.of(2026, 7, 31)
    )

    @Test
    fun `남은 일수로 나눈다 - 오늘 포함`() {
        // 7월 22일 → 남은 일수 10일 (22~31)
        val result = useCase(
            remainingPureBudget = 300_000L,
            today = LocalDate.of(2026, 7, 22),
            budgetPeriod = period
        )

        assertEquals(30_000L, result)
    }

    @Test
    fun `기간 마지막 날에는 남은 금액 전부가 오늘 몫`() {
        val result = useCase(
            remainingPureBudget = 45_000L,
            today = LocalDate.of(2026, 7, 31),
            budgetPeriod = period
        )

        assertEquals(45_000L, result)
    }

    @Test
    fun `첫날에는 전체 일수로 나눈다`() {
        val result = useCase(
            remainingPureBudget = 310_000L,
            today = LocalDate.of(2026, 7, 1),
            budgetPeriod = period
        )

        assertEquals(10_000L, result)
    }

    @Test
    fun `예산을 초과해 남은 금액이 음수면 권장 금액도 음수 - 재분배 대상`() {
        val result = useCase(
            remainingPureBudget = -50_000L,
            today = LocalDate.of(2026, 7, 27),
            budgetPeriod = period
        )

        // 남은 5일에 -50,000 을 나눠 -10,000
        assertEquals(-10_000L, result)
    }

    @Test
    fun `기간이 지난 날짜면 0을 돌려준다`() {
        val result = useCase(
            remainingPureBudget = 100_000L,
            today = LocalDate.of(2026, 8, 5),
            budgetPeriod = period
        )

        assertEquals(0L, result)
    }
}
