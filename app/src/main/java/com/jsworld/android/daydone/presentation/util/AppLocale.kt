package com.jsworld.android.daydone.presentation.util

import java.util.Locale

/**
 * 포맷터·달력·통화가 쓰는 로케일의 단일 출처.
 *
 * 단말 로케일을 그대로 쓰지 않고 **앱이 실제로 문구를 가진 언어**로만 좁힌다.
 * 안 그러면 `localeFilters` 에 없는 언어의 단말에서 문구는 한국어인데 금액만 円이 되는
 * 어긋남이 생긴다. `SUPPORTED` 는 `localeFilters`·`locales_config.xml` 과 **같은 목록**이어야
 * 한다 — 1.6.0 에서 "ja" 를 켤 때 세 곳을 함께 바꾼다.
 */
object AppLocale {
    /** 문구가 있는 언어. localeFilters 와 맞출 것. */
    private val SUPPORTED = setOf("ko", "ja")

    private const val FALLBACK = "ko"

    val current: Locale
        get() = of(Locale.getDefault())

    fun of(device: Locale): Locale {
        val language = if (device.language in SUPPORTED) device.language else FALLBACK
        return when (language) {
            "ja" -> Locale.JAPAN
            else -> Locale.KOREA
        }
    }

    val isJapanese: Boolean
        get() = current.language == "ja"
}
