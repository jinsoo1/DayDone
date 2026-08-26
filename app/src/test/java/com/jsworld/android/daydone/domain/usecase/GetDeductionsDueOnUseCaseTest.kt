package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/** 내일 나갈 돈 알림의 출금일 판정 (docs/v1.4-design.md). */
class GetDeductionsDueOnUseCaseTest {

    private val useCase = GetDeductionsDueOnUseCase()

    private fun deduction(
        id: Long,
        title: String,
        withdrawalDay: Int,
        amount: Long = 100_000L
    ) = ScheduledDeduction(
        id = id,
        title = title,
        amount = amount,
        type = ScheduledDeductionType.FIXED,
        withdrawalDay = withdrawalDay,
        startYearMonth = YearMonth.of(2026, 1),
        endYearMonth = null,
        memo = null
    )

    @Test
    fun `출금일이 그 날짜인 항목만 골라낸다`() {
        val items = listOf(
            deduction(1, "월세", 25),
            deduction(2, "적금", 10),
            deduction(3, "통신비", 25)
        )

        val due = useCase(items, LocalDate.of(2026, 8, 25))

        assertEquals(listOf("월세", "통신비"), due.map { it.title })
    }

    @Test
    fun `출금일이 없는 날은 빈 리스트`() {
        val due = useCase(listOf(deduction(1, "월세", 25)), LocalDate.of(2026, 8, 24))

        assertTrue(due.isEmpty())
    }

    @Test
    fun `출금일 31일은 2월엔 말일에 나간다`() {
        val items = listOf(deduction(1, "적금", 31))

        // 2026년 2월은 28일까지 — 31일 항목은 28일로 clamp
        assertEquals(1, useCase(items, LocalDate.of(2026, 2, 28)).size)
        assertTrue(useCase(items, LocalDate.of(2026, 2, 27)).isEmpty())
    }

    @Test
    fun `출금일 31일은 30일까지인 달엔 30일에 나간다`() {
        val items = listOf(deduction(1, "적금", 31))

        assertEquals(1, useCase(items, LocalDate.of(2026, 4, 30)).size)
        assertTrue(useCase(items, LocalDate.of(2026, 4, 29)).isEmpty())
    }

    @Test
    fun `출금일 31일은 31일까지인 달엔 31일에 나간다`() {
        val items = listOf(deduction(1, "적금", 31))

        assertEquals(1, useCase(items, LocalDate.of(2026, 8, 31)).size)
        assertTrue(useCase(items, LocalDate.of(2026, 8, 30)).isEmpty())
    }

    @Test
    fun `윤년 2월은 29일로 clamp 된다`() {
        val items = listOf(deduction(1, "적금", 30))

        // 2028년은 윤년 — 2월 29일
        assertEquals(1, useCase(items, LocalDate.of(2028, 2, 29)).size)
        assertTrue(useCase(items, LocalDate.of(2028, 2, 28)).isEmpty())
    }
}
