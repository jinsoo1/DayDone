package com.jsworld.android.daydone.presentation.util

import java.util.Locale

/**
 * 포맷터가 쓰는 로케일의 단일 출처.
 *
 * ⚠️ 1.5.0 에서 `Locale.getDefault()` 를 쓰지 않는다 — 앱 문구는 한국어 하나뿐이라
 * (`resConfigs("ko")`) 일본어 단말에서 숫자·요일만 일본식이 되면 문구와 어긋난다.
 * 1.6.0 에서 `values-ja` 와 함께 단말 로케일을 타게 한다.
 */
object AppLocale {
    val current: Locale = Locale.KOREA
}
