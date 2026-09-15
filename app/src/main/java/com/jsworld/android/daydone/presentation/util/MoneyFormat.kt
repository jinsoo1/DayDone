package com.jsworld.android.daydone.presentation.util

import java.text.NumberFormat
import java.util.Locale

/**
 * 통화 표기 규칙.
 *
 * 로케일마다 다른 건 숫자 구분자만이 아니라 **기호의 위치**다.
 * 한국은 뒤(`1,234원`), 일본은 앞(`¥1,234`)과 뒤(`1,234円`)가 모두 쓰인다.
 * 톤상(§13) 일본은 `円` 이 부드러워 뒤가 유력하지만, 앞에 붙는 표기도 담을 수 있게
 * prefix/suffix 를 둘 다 둔다.
 *
 * ⚠️ 1.5.0 은 **한국 값만** 꽂는다. 출력이 1원도 바뀌면 안 된다(회귀 테스트 있음).
 */
enum class CurrencyStyle(
    val locale: Locale,
    val prefix: String,
    val suffix: String
) {
    KRW(Locale.KOREA, "", "원");
    // TODO(1.6.0): JPY(Locale.JAPAN, "", "円")

    companion object {
        val current: CurrencyStyle = KRW
    }
}

fun Long.toMoneyText(style: CurrencyStyle = CurrencyStyle.current): String =
    style.prefix + NumberFormat.getNumberInstance(style.locale).format(this) + style.suffix
