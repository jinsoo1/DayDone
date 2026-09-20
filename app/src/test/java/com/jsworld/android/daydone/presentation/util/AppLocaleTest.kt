package com.jsworld.android.daydone.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * 로케일 분기 — 문구가 있는 언어(SUPPORTED)만 따라가고, 나머지는 한국어로 폴백한다.
 *
 * ⚠️ 1.6.0 에서 SUPPORTED 에 "ja" 를 넣으면 `일본어 단말은 아직 한국어` 케이스가
 * 실패한다. 그건 의도된 실패다 — localeFilters·locales_config 와 함께 세 곳을 바꾸고
 * 그 테스트를 뒤집을 것.
 */
class AppLocaleTest {

    @Test
    fun `한국어 단말은 한국`() {
        assertEquals(Locale.KOREA, AppLocale.of(Locale.KOREA))
        assertEquals(Locale.KOREA, AppLocale.of(Locale("ko", "US")))
    }

    @Test
    fun `지원하지 않는 언어는 한국어로 폴백한다`() {
        assertEquals(Locale.KOREA, AppLocale.of(Locale.US))
        assertEquals(Locale.KOREA, AppLocale.of(Locale.FRANCE))
    }

    @Test
    fun `일본어 단말은 아직 한국어 - ja 를 켜기 전까지`() {
        // localeFilters 에 "ja" 가 없어서 문구가 한국어인데 금액만 円이 되면 안 된다
        assertEquals(Locale.KOREA, AppLocale.of(Locale.JAPAN))
    }

    @Test
    fun `통화와 문장부호는 로케일을 따른다`() {
        assertEquals(CurrencyStyle.KRW, CurrencyStyle.forLocale(Locale.KOREA))
        assertEquals(CurrencyStyle.JPY, CurrencyStyle.forLocale(Locale.JAPAN))
        assertEquals(".", endingMarkFor(Locale.KOREA))
        assertEquals("。", endingMarkFor(Locale.JAPAN))
    }

    @Test
    fun `엔은 뒤에 円 - 소액 상한은 999`() {
        assertEquals("1,234,567円", 1234567L.toMoneyText(CurrencyStyle.JPY))
        assertEquals("0円", 0L.toMoneyText(CurrencyStyle.JPY))
        assertEquals(999L, CurrencyStyle.JPY.smallSpendCeiling)
        assertEquals(9_999L, CurrencyStyle.KRW.smallSpendCeiling)
    }
}
