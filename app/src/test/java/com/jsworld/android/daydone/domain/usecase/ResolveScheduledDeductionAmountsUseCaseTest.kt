package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

/**
 * 고정비/저축 금액의 월별 이월(carry-forward) — §4.
 * "그 달부터 계속 적용, 과거는 보존"이 깨지면 지난 기록이 조용히 바뀐다.
 */
class ResolveScheduledDeductionAmountsUseCaseTest {

    private val useCase = ResolveScheduledDeductionAmountsUseCase()

    private val 적금 = ScheduledDeduction(
        id = 1L,
        title = "적금",
        amount = 500_000L, // 최초 등록 금액
        type = ScheduledDeductionType.SAVING,
        withdrawalDay = 26,
        startYearMonth = YearMonth.of(2026, 5),
        endYearMonth = null,
        memo = null
    )

    @Test
    fun `오버라이드가 없으면 최초 금액을 쓴다`() {
        val result = useCase(
            deductions = listOf(적금),
            overrides = emptyList(),
            anchorMonth = YearMonth.of(2026, 7)
        )

        assertEquals(500_000L, result.single().amount)
    }

    @Test
    fun `8월에 40만으로 바꾸면 7월은 그대로 50만 - 과거 보존`() {
        val overrides = listOf(
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 8), 400_000L)
        )

        val july = useCase(listOf(적금), overrides, YearMonth.of(2026, 7))
        val august = useCase(listOf(적금), overrides, YearMonth.of(2026, 8))

        assertEquals(500_000L, july.single().amount)
        assertEquals(400_000L, august.single().amount)
    }

    @Test
    fun `한 번 바꾼 금액은 다음 달로 이월된다`() {
        val overrides = listOf(
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 8), 400_000L)
        )

        val september = useCase(listOf(적금), overrides, YearMonth.of(2026, 9))

        assertEquals(400_000L, september.single().amount)
    }

    @Test
    fun `오버라이드가 여러 개면 대상 월 이하에서 가장 최근 값이 이긴다`() {
        val overrides = listOf(
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 6), 450_000L),
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 8), 400_000L),
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 11), 300_000L)
        )

        val september = useCase(listOf(적금), overrides, YearMonth.of(2026, 9))

        assertEquals(400_000L, september.single().amount)
    }

    @Test
    fun `미래 달 오버라이드는 현재 달에 영향을 주지 않는다`() {
        val overrides = listOf(
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 12), 100_000L)
        )

        val july = useCase(listOf(적금), overrides, YearMonth.of(2026, 7))

        assertEquals(500_000L, july.single().amount)
    }

    @Test
    fun `다른 항목의 오버라이드가 섞여도 항목별로 정확히 적용된다`() {
        val 월세 = 적금.copy(id = 2L, title = "월세", amount = 600_000L)
        val overrides = listOf(
            ScheduledDeductionAmount(1L, YearMonth.of(2026, 7), 400_000L),
            ScheduledDeductionAmount(2L, YearMonth.of(2026, 7), 650_000L)
        )

        val result = useCase(listOf(적금, 월세), overrides, YearMonth.of(2026, 7))

        assertEquals(400_000L, result.first { it.id == 1L }.amount)
        assertEquals(650_000L, result.first { it.id == 2L }.amount)
    }
}
