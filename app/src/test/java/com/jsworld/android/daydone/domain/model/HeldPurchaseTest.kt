package com.jsworld.android.daydone.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 보류함 30일 룰 파생 (docs/v1.1-design.md §2).
 * 저장 status 는 HELD 그대로 두고, heldAt + 30 ≤ today 면 조회 시 "아낀 돈"으로 파생한다.
 */
class HeldPurchaseTest {

    private val heldAt = LocalDate.of(2026, 8, 1)

    private fun held(
        status: HeldPurchaseStatus = HeldPurchaseStatus.HELD,
        resolvedAt: LocalDate? = null
    ) = HeldPurchase(
        id = 1L,
        title = "무선 이어폰",
        amount = 120_000L,
        heldAt = heldAt,
        status = status,
        resolvedAt = resolvedAt
    )

    @Test
    fun `보류한 날은 0일째이고 30일 남았다`() {
        val item = held()

        assertEquals(0, item.daysHeld(heldAt))
        assertEquals(30, item.daysLeft(heldAt))
        assertTrue(item.isHolding(heldAt))
        assertFalse(item.isSaved(heldAt))
    }

    @Test
    fun `29일째까지는 보류 중이다`() {
        val day29 = heldAt.plusDays(29)
        val item = held()

        assertTrue(item.isHolding(day29))
        assertFalse(item.isAutoPassed(day29))
        assertEquals(1, item.daysLeft(day29))
    }

    @Test
    fun `30일이 지나면 자동으로 아낀 돈이 된다`() {
        val day30 = heldAt.plusDays(30)
        val item = held()

        assertFalse(item.isHolding(day30))
        assertTrue(item.isAutoPassed(day30))
        assertTrue(item.isSaved(day30))
    }

    @Test
    fun `직접 안 사기로 확정하면 자동 전환과 무관하게 아낀 돈이다`() {
        val item = held(
            status = HeldPurchaseStatus.PASSED,
            resolvedAt = heldAt.plusDays(3)
        )

        assertTrue(item.isSaved(heldAt.plusDays(3)))
        assertFalse(item.isHolding(heldAt.plusDays(3)))
        assertEquals(3, item.daysToResolve())
    }

    @Test
    fun `결국 산 항목은 아낀 돈이 아니다`() {
        val item = held(
            status = HeldPurchaseStatus.BOUGHT,
            resolvedAt = heldAt.plusDays(40)
        )

        assertFalse(item.isSaved(heldAt.plusDays(40)))
        assertFalse(item.isAutoPassed(heldAt.plusDays(40)))
        assertEquals(40, item.daysToResolve())
    }
}
