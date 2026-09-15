package com.jsworld.android.daydone.notification

import android.content.Context
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.NotificationText
import com.jsworld.android.daydone.presentation.util.text
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.presentation.util.withEndingMark
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * [NotificationText] → 실제 문장.
 *
 * UseCase 는 무엇을 말할지만 정하고(판정), 문장 조립은 여기서 한다(표시).
 * Context 가 있는 쪽에서 조립해야 `getString` 으로 로케일을 탈 수 있다.
 */
@Singleton
class NotificationTextRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun render(text: NotificationText): String = with(context) {
        when (text) {
            is NotificationText.MorningTitle ->
                getString(R.string.noti_morning_title, text.todayRecommended.toMoneyText())

            NotificationText.MorningTitleNoBudget ->
                getString(R.string.noti_morning_title_no_budget)

            NotificationText.MorningReportLine ->
                getString(R.string.noti_morning_report_line)

            is NotificationText.MorningRemainingDays ->
                getString(R.string.noti_morning_remaining_days, text.remainingDays)

            NotificationText.NoSpendCelebrationTitle ->
                getString(R.string.noti_no_spend_title)

            is NotificationText.NoSpendCelebrationStreakBody ->
                getString(R.string.noti_no_spend_body_streak, text.streak)

            NotificationText.NoSpendCelebrationFirstBody ->
                getString(R.string.noti_no_spend_body_first)

            NotificationText.RecordNoticeTitle -> getString(R.string.noti_record_title)
            NotificationText.RecordNoticeBody -> getString(R.string.noti_record_body)

            is NotificationText.UpcomingDeductionTitle ->
                if (text.count == 1) {
                    getString(
                        R.string.noti_upcoming_title_single,
                        text.firstTitle,
                        text.totalAmount.toMoneyText()
                    )
                } else {
                    getString(
                        R.string.noti_upcoming_title_multi,
                        text.count,
                        text.totalAmount.toMoneyText()
                    )
                }

            is NotificationText.UpcomingDeductionBody ->
                if (text.names.size <= 1) {
                    getString(R.string.noti_upcoming_body)
                } else {
                    getString(
                        R.string.noti_upcoming_body_multi,
                        text.names.joinToString(getString(R.string.noti_upcoming_name_separator))
                    )
                }

            is NotificationText.HeldPurchaseTitle ->
                if (text.count == 1) {
                    getString(R.string.noti_held_title_single, text.firstTitle)
                } else {
                    getString(R.string.noti_held_title_multi, text.count)
                }

            is NotificationText.HeldPurchaseBody ->
                getString(R.string.noti_held_body, text.totalAmount.toMoneyText())

            is NotificationText.Joined ->
                text.parts.joinToString(getString(R.string.noti_join_separator)) { render(it) }

            is NotificationText.AsSentence -> render(text.text).withEndingMark()

            is NotificationText.BigSpendMonth -> text.notice.text(context)
        }
    }
}
