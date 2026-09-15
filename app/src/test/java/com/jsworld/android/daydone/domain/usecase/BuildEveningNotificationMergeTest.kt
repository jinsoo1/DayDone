package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.domain.model.NotificationText
import com.jsworld.android.daydone.notification.NotificationTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 저녁 알림 합치기 (docs/v1.4-design.md §2).
 *
 * 기록 안내와 "내일 나갈 돈"은 따로 켤 수 있고, 같은 저녁에 겹치면 한 건으로 합쳐진다.
 *
 * ⚠️ v1.5 국제화 3-3 에서 문구가 리소스로 나가면서 **문자열 비교 → 타입 비교**로 바꿨다.
 * 검증 대상은 문구가 아니라 **제목 우선순위와 합치기 규칙**이다. 그건 그대로 남는다.
 */
class BuildEveningNotificationMergeTest {

    private val recordNotice = BuildEveningNotificationUseCase.RecordNotice(
        content = NotificationContent(
            title = NotificationText.RecordNoticeTitle,
            body = NotificationText.RecordNoticeBody,
            target = NotificationTarget.EXPENSE_INPUT
        ),
        isCelebration = false
    )

    private val celebration = BuildEveningNotificationUseCase.RecordNotice(
        content = NotificationContent(
            title = NotificationText.NoSpendCelebrationTitle,
            body = NotificationText.NoSpendCelebrationStreakBody(streak = 3),
            target = NotificationTarget.TODAY
        ),
        isCelebration = true
    )

    private val upcomingTitle = NotificationText.UpcomingDeductionTitle(
        count = 1,
        firstTitle = "월세",
        totalAmount = 500_000L
    )

    private val upcoming = NotificationContent(
        title = upcomingTitle,
        body = NotificationText.UpcomingDeductionBody(names = listOf("월세"))
    )

    /** 합쳐진 본문을 이루는 조각들. 합쳐지지 않았으면 그 하나. */
    private fun NotificationText.parts(): List<NotificationText> =
        if (this is NotificationText.Joined) parts else listOf(this)

    @Test
    fun `둘 다 없으면 보내지 않는다`() {
        assertNull(BuildEveningNotificationUseCase.merge(record = null, upcoming = null))
    }

    @Test
    fun `기록 안내만 있으면 그대로 보낸다`() {
        val merged = BuildEveningNotificationUseCase.merge(recordNotice, null)

        assertEquals(NotificationText.RecordNoticeTitle, merged!!.title)
        assertEquals(NotificationTarget.EXPENSE_INPUT, merged.target)
    }

    @Test
    fun `기록 안내를 꺼도 내일 출금 안내만으로 보낸다`() {
        val merged = BuildEveningNotificationUseCase.merge(record = null, upcoming = upcoming)

        assertEquals(upcomingTitle, merged!!.title)
        // 출금 안내만 있을 땐 지출 입력 시트를 열 이유가 없다
        assertEquals(NotificationTarget.TODAY, merged.target)
    }

    @Test
    fun `둘 다 있으면 한 건으로 합치고 출금 안내를 제목으로 쓴다`() {
        val merged = BuildEveningNotificationUseCase.merge(recordNotice, upcoming)!!

        assertEquals(upcomingTitle, merged.title)

        val parts = merged.body.parts()
        assertTrue(parts.any { it is NotificationText.UpcomingDeductionBody })
        assertTrue(parts.contains(NotificationText.RecordNoticeBody))
        assertEquals(NotificationTarget.EXPENSE_INPUT, merged.target)
    }

    @Test
    fun `무지출 축하는 출금 안내에 밀리지 않는다`() {
        val merged = BuildEveningNotificationUseCase.merge(celebration, upcoming)!!

        assertEquals(NotificationText.NoSpendCelebrationTitle, merged.title)

        // 축하가 제목을 지키되 출금 사실은 본문에 담긴다
        val parts = merged.body.parts()
        assertEquals(NotificationText.NoSpendCelebrationStreakBody(3), parts[0])
        // 출금 안내는 제목이 본문으로 내려오므로 문장부호를 맞춰 잇는다
        assertEquals(NotificationText.AsSentence(upcomingTitle), parts[1])
        assertTrue(parts[2] is NotificationText.UpcomingDeductionBody)
        assertEquals(NotificationTarget.TODAY, merged.target)
    }
}
