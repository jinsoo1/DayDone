package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.notification.NotificationTarget
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * 저녁 알림 한 건을 만든다 (docs/v1.4-design.md §2).
 *
 * 기록 안내와 "내일 나갈 돈"이 같은 저녁에 겹치면 **한 건으로 합친다** — 아침 알림과 같은 규칙.
 * 두 알림은 서로 독립적으로 켤 수 있어서, 기록 안내를 꺼도 내일 출금 안내만 받을 수 있다.
 * 보낼 내용이 없으면 null.
 *
 * 제목 우선순위: 무지출 성공 축하 > 내일 출금(통장 확인이 필요한 행동) > 기록 안내.
 * 축하해야 할 순간을 다른 안내가 잡아먹지 않게 한다.
 */
class BuildEveningNotificationUseCase @Inject constructor(
    private val observeExpensesByPeriodUseCase: ObserveExpensesByPeriodUseCase,
    private val observeNoSpendChallengeUseCase: ObserveNoSpendChallengeUseCase,
    private val evaluateNoSpendProgressUseCase: EvaluateNoSpendProgressUseCase,
    private val buildUpcomingDeductionNoticeUseCase: BuildUpcomingDeductionNoticeUseCase
) {
    suspend operator fun invoke(
        today: LocalDate,
        settings: NotificationSettings
    ): NotificationContent? {
        val record = if (settings.eveningEnabled) buildRecordNotice(today) else null
        val upcoming = if (settings.upcomingDeductionEnabled) {
            buildUpcomingDeductionNoticeUseCase(today)
        } else {
            null
        }

        return merge(record, upcoming)
    }

    /** 기록 안내(또는 무지출 축하). 이미 기록이 있으면 null. */
    private suspend fun buildRecordNotice(today: LocalDate): RecordNotice? {
        val todayExpenses = observeExpensesByPeriodUseCase(today, today).first()

        // 이미 기록이 있으면 보내지 않는다
        if (todayExpenses.any { it.type == ExpenseType.GENERAL }) return null

        val challenge = observeNoSpendChallengeUseCase().first()
        val start = challenge.startDate

        if (challenge.enabled && start != null && challenge.targetDays > 0) {
            val end = start.plusDays((challenge.targetDays - 1).toLong())
            val inWindow = !today.isBefore(start) && !today.isAfter(end)

            if (inWindow) {
                val windowExpenses = observeExpensesByPeriodUseCase(start, end).first()
                val progress = evaluateNoSpendProgressUseCase(
                    expenses = windowExpenses,
                    settings = challenge,
                    today = today
                )

                // 챌린지 성공 중인 날 — 입력 요청 없이 축하만
                if (progress.isTodayOnTrack) {
                    return RecordNotice(
                        content = NotificationContent(
                            title = "오늘 지갑이 쉬었어요 🎉",
                            body = if (progress.streak >= 2) {
                                "무지출 ${progress.streak}일째, 잘하고 있어요."
                            } else {
                                "무지출 하루 성공. 내일도 편하게 가요."
                            },
                            target = NotificationTarget.TODAY
                        ),
                        isCelebration = true
                    )
                }

                // CAP 모드처럼 지출이 허용되는 경우엔 평소 문구로 내려간다
                if (challenge.mode == NoSpendMode.FULL) return null
            }
        }

        return RecordNotice(
            content = NotificationContent(
                title = "오늘 기록이 아직 없어요",
                body = "쓴 게 있다면 지금 넣어두면 내일 금액이 정확해져요. " +
                        "안 썼다면 그대로 두셔도 좋아요.",
                target = NotificationTarget.EXPENSE_INPUT
            ),
            isCelebration = false
        )
    }

    internal data class RecordNotice(
        val content: NotificationContent,
        val isCelebration: Boolean
    )

    internal companion object {

        /**
         * 저녁에 겹친 안내를 한 건으로 합친다.
         *
         * 제목 우선순위: 무지출 축하 > 내일 출금 > 기록 안내.
         * 축하해야 할 순간을 다른 안내가 잡아먹지 않게 하고, 그 외에는 지금 행동이 필요한
         * 출금 안내(통장 확인)를 앞세운다.
         */
        internal fun merge(
            record: RecordNotice?,
            upcoming: NotificationContent?
        ): NotificationContent? = when {
            record == null -> upcoming
            upcoming == null -> record.content

            record.isCelebration -> NotificationContent(
                title = record.content.title,
                body = "${record.content.body} ${upcoming.title.withEndingPeriod()} ${upcoming.body}",
                target = NotificationTarget.TODAY
            )

            else -> NotificationContent(
                title = upcoming.title,
                body = "${upcoming.body} ${record.content.body}",
                target = NotificationTarget.EXPENSE_INPUT
            )
        }

        /** 제목을 본문 문장으로 이어 쓸 때 문장부호를 맞춘다. */
        private fun String.withEndingPeriod(): String =
            if (endsWith(".") || endsWith("!") || endsWith("?")) this else "$this."
    }
}
