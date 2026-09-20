package com.jsworld.android.daydone.presentation.util

import java.text.NumberFormat
import java.util.Locale

/**
 * 통화 표기 규칙.
 *
 * 로케일마다 다른 건 숫자 구분자만이 아니라 **기호의 위치**다.
 * 한국은 뒤(`1,234원`), 일본은 앞(`¥1,234`)과 뒤(`1,234円`)가 모두 쓰인다.
 * 톤상(§13) 일본은 `円` 이 부드러워 뒤에 붙인다. `¥` 앞 표기는 가격표 느낌이라 차갑다.
 *
 * [smallSpendCeiling] 은 리포트의 "소액 다건" 판정 상한 — 통화 무관 숫자로 두면
 * 9,999円(약 9만 원)이 "소액"이 된다. 문구(`report_advice_small_spends`)와 함께 맞춘다.
 */
enum class CurrencyStyle(
    val locale: Locale,
    val prefix: String,
    val suffix: String,
    val smallSpendCeiling: Long
) {
    KRW(Locale.KOREA, "", "원", smallSpendCeiling = 9_999L),
    JPY(Locale.JAPAN, "", "円", smallSpendCeiling = 999L);

    companion object {
        val current: CurrencyStyle
            get() = forLocale(AppLocale.current)

        fun forLocale(locale: Locale): CurrencyStyle =
            if (locale.language == "ja") JPY else KRW
    }
}

fun Long.toMoneyText(style: CurrencyStyle = CurrencyStyle.current): String =
    style.prefix + NumberFormat.getNumberInstance(style.locale).format(this) + style.suffix
