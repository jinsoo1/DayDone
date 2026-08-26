package com.jsworld.android.daydone.domain.model

import com.jsworld.android.daydone.notification.NotificationTarget

/**
 * 보낼 알림 한 건. 내용 결정은 UseCase 가, 표시는 Notifier 가 한다.
 *
 * 여러 안내가 한 건으로 합쳐지면 눌렀을 때 갈 화면도 달라지므로(예: 저녁 알림이
 * 기록 안내 없이 출금 안내만 담고 있으면 지출 입력 시트를 열 이유가 없다)
 * 목적지도 내용과 함께 정한다.
 */
data class NotificationContent(
    val title: String,
    val body: String,
    val target: NotificationTarget = NotificationTarget.TODAY
)
