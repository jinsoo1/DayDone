package com.jsworld.android.daydone.widget

import androidx.compose.ui.graphics.Color

/**
 * 위젯 전용 색 (라이트/다크 쌍).
 * 위젯은 런처 위에 놓여 앱 테마가 닿지 않으므로 여기서 쌍으로 관리한다.
 * 값은 앱 팔레트(ui/theme/Color.kt)와 같은 것을 쓴다.
 */
internal object WidgetColors {
    val backgroundLight = Color(0xFFFFFFFF)
    val backgroundDark = Color(0xFF23242B)

    val accentLight = Color(0xFF3182F6)   // TossBlue
    val accentDark = Color(0xFF5A9BFF)    // TossBlueOnDark

    val textLight = Color(0xFF191F28)     // Ink
    val textDark = Color(0xFFF2F4F6)      // DarkText

    val subTextLight = Color(0xFF8B95A1)  // GrayText
    val subTextDark = Color(0xFF9AA3AD)   // DarkGrayText

    val trackLight = Color(0xFFF2F4F6)    // BgGray
    val trackDark = Color(0xFF2E2F38)     // DarkLine
}
