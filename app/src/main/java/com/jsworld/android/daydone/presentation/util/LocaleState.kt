package com.jsworld.android.daydone.presentation.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * "지금 로케일"을 흐름으로 든다 — ViewModel 이 만든 문구를 언어 변경 뒤 다시 조립하기 위해.
 *
 * 단말 언어를 바꾸면 Activity 는 다시 만들어지지만 **ViewModel 은 살아남고**, 그 안의
 * UiState 에 `getString` 으로 조립해둔 문구(오늘 권장 메시지·"오늘 내역"·월 제목)는
 * 옛 언어 그대로 남는다. 화면의 `stringResource` 는 새 언어라 두 언어가 섞여 보였다.
 * 각 ViewModel 의 조회 파이프라인이 이 흐름을 `combine` 해 값이 바뀌면 다시 그린다.
 *
 * 갱신은 `MainActivity.onCreate` — 언어 변경은 Activity 재생성을 동반하므로 거기서 잡힌다.
 */
object LocaleState {
    private val _flow = MutableStateFlow(AppLocale.current)
    val flow: StateFlow<Locale> = _flow

    /** 바뀌었으면 true. */
    fun refresh(): Boolean {
        val now = AppLocale.current
        if (now == _flow.value) return false
        _flow.value = now
        return true
    }
}
