package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.PurchaseImpact
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 살까 말까 계산 (docs/v1.1-design.md §1).
 * A = R ÷ D, B = (R − P) ÷ D.
 * 거의 무변화(감소 < 5%) / 여유(B ≥ 70%A) / 빠듯(0 ≤ B < 70%A) / 불가(R − P < 0).
 */
class EvaluatePurchaseUseCaseTest {

    private val useCase = EvaluatePurchaseUseCase()

    @Test
    fun `하루 권장은 오늘 탭과 같은 나눗셈 - R 나누기 D`() {
        // R=320,000, D=10 → A=32,000 / P=80,000 → B=24,000
        val result = useCase(pureBudgetLeft = 320_000L, remainingDays = 10, price = 80_000L)

        assertEquals(32_000L, result.currentDaily)
        assertEquals(24_000L, result.afterDaily)
    }

    @Test
    fun `감소가 5% 미만이면 거의 무변화`() {
        // A=100,000, P=40,000/10일 → B=96,000, 감소 4%
        val result = useCase(pureBudgetLeft = 1_000_000L, remainingDays = 10, price = 40_000L)

        assertEquals(PurchaseImpact.NEGLIGIBLE, result.impact)
    }

    @Test
    fun `감소가 정확히 5%면 무변화가 아니라 여유`() {
        // A=100,000, P=50,000/10일 → B=95,000, 감소 5% (경계: 미만만 무변화)
        val result = useCase(pureBudgetLeft = 1_000_000L, remainingDays = 10, price = 50_000L)

        assertEquals(PurchaseImpact.COMFORTABLE, result.impact)
    }

    @Test
    fun `구매 후 권장이 70% 이상이면 여유`() {
        // A=32,000, B=24,000 → 75%
        val result = useCase(pureBudgetLeft = 320_000L, remainingDays = 10, price = 80_000L)

        assertEquals(PurchaseImpact.COMFORTABLE, result.impact)
    }

    @Test
    fun `구매 후 권장이 정확히 70%면 여유 - 경계 포함`() {
        // A=100,000, B=70,000
        val result = useCase(pureBudgetLeft = 1_000_000L, remainingDays = 10, price = 300_000L)

        assertEquals(PurchaseImpact.COMFORTABLE, result.impact)
    }

    @Test
    fun `구매 후 권장이 70% 미만이면 빠듯`() {
        // A=100,000, B=69,900
        val result = useCase(pureBudgetLeft = 1_000_000L, remainingDays = 10, price = 301_000L)

        assertEquals(PurchaseImpact.TIGHT, result.impact)
    }

    @Test
    fun `가격이 남은 생활비와 정확히 같으면 B는 0원이고 빠듯`() {
        val result = useCase(pureBudgetLeft = 320_000L, remainingDays = 10, price = 320_000L)

        assertEquals(0L, result.afterDaily)
        assertEquals(PurchaseImpact.TIGHT, result.impact)
    }

    @Test
    fun `가격이 남은 생활비보다 크면 불가`() {
        val result = useCase(pureBudgetLeft = 320_000L, remainingDays = 10, price = 320_001L)

        assertEquals(PurchaseImpact.IMPOSSIBLE, result.impact)
    }

    @Test
    fun `기간 마지막 날에도 정상 계산 - D는 1`() {
        // "오늘 하루 권장이 32,000원 → 2,000원이 돼요"
        val result = useCase(pureBudgetLeft = 32_000L, remainingDays = 1, price = 30_000L)

        assertEquals(32_000L, result.currentDaily)
        assertEquals(2_000L, result.afterDaily)
        assertEquals(PurchaseImpact.TIGHT, result.impact)
    }

    @Test
    fun `남은 생활비가 0이면 어떤 가격이든 불가`() {
        val result = useCase(pureBudgetLeft = 0L, remainingDays = 10, price = 1_000L)

        assertEquals(PurchaseImpact.IMPOSSIBLE, result.impact)
    }

    @Test
    fun `이미 예산을 초과한 상태면 불가`() {
        val result = useCase(pureBudgetLeft = -50_000L, remainingDays = 5, price = 10_000L)

        assertEquals(PurchaseImpact.IMPOSSIBLE, result.impact)
    }

    @Test
    fun `남은 일수가 0 이하로 들어와도 1일로 계산한다`() {
        val result = useCase(pureBudgetLeft = 30_000L, remainingDays = 0, price = 10_000L)

        assertEquals(30_000L, result.currentDaily)
        assertEquals(20_000L, result.afterDaily)
    }
}
