package com.jsworld.android.daydone.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 제목을 본문으로 이어 쓸 때의 문장부호 규칙.
 *
 * 저녁 알림 합치기(무지출 축하 + 내일 출금)에서만 쓰지만, 원래 `BuildEveningNotificationUseCase`
 * 안에 있던 규칙이라 리소스로 문구가 나간 뒤에도 테스트가 따라가야 한다.
 */
class SentenceFormatTest {

    @Test
    fun `마침표가 없으면 붙인다`() {
        assertEquals("내일 월세 500,000원이 나가요.", "내일 월세 500,000원이 나가요".withEndingMark())
    }

    @Test
    fun `이미 문장부호로 끝나면 그대로 둔다`() {
        assertEquals("오늘 지갑이 쉬었어요!", "오늘 지갑이 쉬었어요!".withEndingMark())
        assertEquals("그럴까요?", "그럴까요?".withEndingMark())
        assertEquals("끝났어요.", "끝났어요.".withEndingMark())
    }

    @Test
    fun `일본어 문장부호도 문장 끝으로 인정한다`() {
        // 1.6.0 에서 마침표만 바꾸면 되도록 미리 열어둔다
        assertEquals("終わりました。", "終わりました。".withEndingMark())
        assertEquals("終わりました。", "終わりました".withEndingMark("。"))
    }

    @Test
    fun `빈 문자열엔 붙이지 않는다`() {
        assertEquals("", "".withEndingMark())
    }
}
