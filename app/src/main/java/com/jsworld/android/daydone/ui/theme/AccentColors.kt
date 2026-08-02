package com.jsworld.android.daydone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Material 컬러스킴에 없는 "지켜냈다/성공" 계열 초록 강조색.
 * 화면에서 하드코딩하지 않고 여기서만 관리해 라이트·다크를 함께 맞춘다.
 */
object DayDoneAccent {

    /** 남긴 금액 등 긍정 수치 강조 텍스트 */
    val successText: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF5DCAA5) else Color(0xFF0F6E56)

    /** 제안·팁 박스 배경 */
    val successContainer: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF10352C) else Color(0xFFE1F5EE)

    /** 제안·팁 박스 위의 글자 */
    val onSuccessContainer: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF9FE1CB) else Color(0xFF085041)

    /** 캘린더 무지출 성공 체크 */
    val noSpendCheck: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF5DCAA5) else Color(0xFF2E9E5B)
}
