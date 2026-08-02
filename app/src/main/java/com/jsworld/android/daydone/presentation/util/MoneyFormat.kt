package com.jsworld.android.daydone.presentation.util

import java.text.NumberFormat
import java.util.Locale

fun Long.toMoneyText(): String {
    return NumberFormat
        .getNumberInstance(Locale.KOREA)
        .format(this) + "원"
}