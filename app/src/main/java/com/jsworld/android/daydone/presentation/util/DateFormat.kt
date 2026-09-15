package com.jsworld.android.daydone.presentation.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 요일 한 글자.
 *
 * 같은 로직이 세 곳에 복사돼 있었다(`LedgerViewModel` · `GetTodayDateChipsUseCase` ·
 * `BuildMonthlyReportUseCase`). `java.time` 의 `getDisplayName` 이 ko/ja 모두
 * 올바른 한 글자를 주므로 when 표를 직접 들고 있을 이유가 없다.
 *
 * ⚠️ domain 쪽 사본 2개는 여기서 지우지 않는다 — 그 문장들이 sealed 타입이 되면서
 * (v1.5 설계 3-5·3-6) 호출부와 함께 사라진다.
 */
fun DayOfWeek.toWeekText(locale: Locale = AppLocale.current): String =
    getDisplayName(TextStyle.SHORT, locale)

fun LocalDate.toWeekText(locale: Locale = AppLocale.current): String =
    dayOfWeek.toWeekText(locale)
