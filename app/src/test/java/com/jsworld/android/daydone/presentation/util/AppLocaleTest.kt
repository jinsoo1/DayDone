package com.jsworld.android.daydone.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * 로케일 분기 — 문구가 있는 언어(SUPPORTED)만 따라가고, 나머지는 한국어로 폴백한다.
 *
 * SUPPORTED 는 localeFilters·locales_config.xml 과 같은 목록이어야 한다. 새 언어를
 * 켤 때 세 곳을 한 커밋에 바꾸고 여기 케이스를 더한다.
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
    fun `일본어 단말은 일본`() {
        assertEquals(Locale.JAPAN, AppLocale.of(Locale.JAPAN))
        assertEquals(Locale.JAPAN, AppLocale.of(Locale("ja")))
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
