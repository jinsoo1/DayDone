package com.jsworld.android.daydone.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * v1.5.0 국제화 기반 — **한국어 출력이 한 글자도 바뀌지 않는 것**을 고정한다.
 *
 * 통화 기호 위치 개념(prefix/suffix)을 넣으면서 기존 `NumberFormat + "원"` 출력이
 * 미세하게 달라지는 게 이 작업의 유일한 유저 체감 위험이다.
 */
class MoneyFormatTest {

    @Test
    fun `금액은 세 자리 구분 + 원`() {
        assertEquals("1,234,567원", 1234567L.toMoneyText())
        assertEquals("1,000원", 1000L.toMoneyText())
        assertEquals("999원", 999L.toMoneyText())
    }

    @Test
    fun `0과 음수도 기존 출력 그대로`() {
        assertEquals("0원", 0L.toMoneyText())
        // 내역 화면 "남은 금액"이 초과면 음수로 내려온다(§12 — 사실대로 쓴다)
        assertEquals("-5,400원", (-5400L).toMoneyText())
    }

    @Test
    fun `통화 표기는 prefix와 suffix를 모두 붙인다`() {
        // 1.6.0 에서 ¥ 앞 표기를 꽂아도 조립 규칙이 그대로임을 고정
        assertEquals("", CurrencyStyle.KRW.prefix)
        assertEquals("원", CurrencyStyle.KRW.suffix)
        assertEquals(CurrencyStyle.KRW, CurrencyStyle.current)
    }

    @Test
    fun `요일은 한 글자`() {
        // 2026-09-14 월요일
        val monday = LocalDate.of(2026, 9, 14)
        assertEquals("월", monday.toWeekText())
        assertEquals("일", monday.plusDays(6).toWeekText())
    }
}
