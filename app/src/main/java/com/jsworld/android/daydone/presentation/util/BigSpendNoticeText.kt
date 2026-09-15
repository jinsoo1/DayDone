package com.jsworld.android.daydone.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.BigSpendNotice
import com.jsworld.android.daydone.domain.model.KoreanLunarHoliday

/**
 * [BigSpendNotice] → 실제 문장.
 *
 * 월 탭 한 줄과 아침 알림이 **같은 안내**를 쓰므로 조립도 한 곳에서 한다 —
 * 두 벌로 두면 문구가 갈라진다(§12 "같은 UseCase 파생이라 알림과 문구가 항상 일치한다").
 * Composable 이 아닌 Context 기반이라 ViewModel·워커 양쪽에서 쓸 수 있다.
 */
fun BigSpendNotice.text(context: Context): String = when (this) {
    is BigSpendNotice.LunarHoliday ->
        context.getString(R.string.big_spend_lunar, context.getString(holiday.labelRes))

    BigSpendNotice.FamilyMonth -> context.getString(R.string.big_spend_family_month)
    BigSpendNotice.YearEnd -> context.getString(R.string.big_spend_year_end)
}

@get:StringRes
private val KoreanLunarHoliday.labelRes: Int
    get() = when (this) {
        KoreanLunarHoliday.SEOLLAL -> R.string.holiday_seollal
        KoreanLunarHoliday.CHUSEOK -> R.string.holiday_chuseok
    }
