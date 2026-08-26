package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.notification.NotificationTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 저녁 알림 합치기 (docs/v1.4-design.md §2).
 *
 * 기록 안내와 "내일 나갈 돈"은 따로 켤 수 있고, 같은 저녁에 겹치면 한 건으로 합쳐진다.
 */
class BuildEveningNotificationMergeTest {

    private val recordNotice = BuildEveningNotificationUseCase.RecordNotice(
        content = NotificationContent(
            title = "오늘 기록이 아직 없어요",
            body = "쓴 게 있다면 지금 넣어두면 내일 금액이 정확해져요.",
            target = NotificationTarget.EXPENSE_INPUT
        ),
        isCelebration = false
    )

    private val celebration = BuildEveningNotificationUseCase.RecordNotice(
        content = NotificationContent(
            title = "오늘 지갑이 쉬었어요 🎉",
            body = "무지출 3일째, 잘하고 있어요.",
            target = NotificationTarget.TODAY
        ),
        isCelebration = true
    )

    private val upcoming = NotificationContent(
        title = "내일 월세 500,000원이 나가요",
        body = "이미 생활비에선 빼둔 돈이라 통장만 확인해두면 돼요."
    )

    @Test
    fun `둘 다 없으면 보내지 않는다`() {
        assertNull(BuildEveningNotificationUseCase.merge(record = null, upcoming = null))
    }

    @Test
    fun `기록 안내만 있으면 그대로 보낸다`() {
        val merged = BuildEveningNotificationUseCase.merge(recordNotice, null)

        assertEquals("오늘 기록이 아직 없어요", merged!!.title)
        assertEquals(NotificationTarget.EXPENSE_INPUT, merged.target)
    }

    @Test
    fun `기록 안내를 꺼도 내일 출금 안내만으로 보낸다`() {
        val merged = BuildEveningNotificationUseCase.merge(record = null, upcoming = upcoming)

        assertEquals("내일 월세 500,000원이 나가요", merged!!.title)
        // 출금 안내만 있을 땐 지출 입력 시트를 열 이유가 없다
        assertEquals(NotificationTarget.TODAY, merged.target)
    }

    @Test
    fun `둘 다 있으면 한 건으로 합치고 출금 안내를 제목으로 쓴다`() {
        val merged = BuildEveningNotificationUseCase.merge(recordNotice, upcoming)!!

        assertEquals("내일 월세 500,000원이 나가요", merged.title)
        assertTrue(merged.body.contains("통장만 확인"))
        assertTrue(merged.body.contains("내일 금액이 정확해져요"))
        assertEquals(NotificationTarget.EXPENSE_INPUT, merged.target)
    }

    @Test
    fun `무지출 축하는 출금 안내에 밀리지 않는다`() {
        val merged = BuildEveningNotificationUseCase.merge(celebration, upcoming)!!

        assertEquals("오늘 지갑이 쉬었어요 🎉", merged.title)
        // 축하가 제목을 지키되 출금 사실은 본문에 담긴다
        assertTrue(merged.body.contains("무지출 3일째"))
        assertTrue(merged.body.contains("내일 월세 500,000원이 나가요."))
        assertTrue(merged.body.contains("통장만 확인"))
        assertEquals(NotificationTarget.TODAY, merged.target)
    }
}
