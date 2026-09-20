package com.jsworld.android.daydone.presentation.util

/**
 * 제목을 본문 문장으로 이어 쓸 때 문장부호를 맞춘다.
 *
 * 저녁 알림에서 무지출 축하가 제목을 지키고 출금 안내가 본문으로 내려갈 때 쓴다 —
 * 원래 제목이라 마침표가 없어서 그냥 이으면 두 문장이 붙어버린다.
 *
 * 문장부호는 로케일을 탄다 — 일본어는 `。`. 이미 어느 쪽 부호로든 끝나 있으면 그대로.
 * Android 의존이 없는 순수 함수라 유닛 테스트로 지킬 수 있다.
 */
fun String.withEndingMark(mark: String = endingMarkFor(AppLocale.current)): String =
    if (isEmpty() || ENDING_MARKS.any { endsWith(it) }) this else this + mark

fun endingMarkFor(locale: java.util.Locale): String =
    if (locale.language == "ja") "。" else "."

private val ENDING_MARKS = listOf(".", "!", "?", "。", "！", "？")
