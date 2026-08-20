package com.jsworld.android.daydone.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.view.MainActivity

private val accent = ColorProvider(WidgetColors.accentLight, WidgetColors.accentDark)
private val onSurface = ColorProvider(WidgetColors.textLight, WidgetColors.textDark)
private val subText = ColorProvider(WidgetColors.subTextLight, WidgetColors.subTextDark)
private val surface = ColorProvider(WidgetColors.backgroundLight, WidgetColors.backgroundDark)
private val track = ColorProvider(WidgetColors.trackLight, WidgetColors.trackDark)

/**
 * 홈 위젯 "오늘 권장 금액" (docs/v1.3-design.md §2).
 *
 * - 2×2: 숫자 하나 + 진행 바 + 남은 일수 ("숫자 하나"가 위젯의 미덕)
 * - 4×2: 위 + 기간 · 내일 금액 · 오늘 사용액
 * - 초과여도 빨간 숫자를 쓰지 않는다 — 대신 내일 권장 금액을 보여준다.
 */
object DayDoneWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // 화면 안에서 구독한다. 세션이 살아 있는 동안에도 데이터가 바뀌면
            // 재구성으로 곧바로 따라온다 (값 하나를 미리 읽어 넘기면 갱신이 밀린다).
            val state by remember { widgetStateFlow(context) }
                .collectAsState(initial = null)

            GlanceTheme {
                WidgetBody(state ?: DayDoneWidgetState.Loading)
            }
        }
    }
}

@Composable
private fun WidgetBody(state: DayDoneWidgetState) {
    val isWide = LocalSize.current.width >= 260.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surface)
            .cornerRadius(20.dp)
            .padding(if (isWide) 18.dp else 16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        when (state) {
            DayDoneWidgetState.Loading -> MessageBody(
                title = "불러오는 중이에요",
                sub = ""
            )

            DayDoneWidgetState.NeedsSetup -> MessageBody(
                title = "예산을 먼저 설정해 주세요",
                sub = "탭하면 데이던이 열려요"
            )

            DayDoneWidgetState.Unavailable -> MessageBody(
                title = "잠시 후 다시 보여드릴게요",
                sub = "탭하면 오늘 금액을 볼 수 있어요"
            )
            is DayDoneWidgetState.Ready -> if (isWide) WideBody(state) else CompactBody(state)
        }
    }
}

@Composable
private fun MessageBody(title: String, sub: String) {
    Text(
        text = title,
        style = TextStyle(fontSize = 14.sp, color = onSurface, fontWeight = FontWeight.Medium)
    )
    Spacer(GlanceModifier.height(4.dp))
    Text(text = sub, style = TextStyle(fontSize = 12.sp, color = subText))
}

/** 2×2 — 숫자 하나. */
@Composable
private fun CompactBody(state: DayDoneWidgetState.Ready) {
    Text(text = state.label, style = TextStyle(fontSize = 12.sp, color = subText))
    Spacer(GlanceModifier.height(6.dp))
    Text(
        text = state.amount.toMoneyText(),
        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
    )
    Spacer(GlanceModifier.height(10.dp))
    LinearProgressIndicator(
        progress = state.progress,
        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        color = accent,
        backgroundColor = track
    )
    Spacer(GlanceModifier.height(6.dp))
    Text(
        text = "${state.remainingDays}일 남음",
        style = TextStyle(fontSize = 11.sp, color = subText)
    )
}

/** 4×2 — 기간·내일 금액·오늘 사용액까지. */
@Composable
private fun WideBody(state: DayDoneWidgetState.Ready) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = state.label,
            style = TextStyle(fontSize = 12.sp, color = subText),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(text = state.periodText, style = TextStyle(fontSize = 11.sp, color = subText))
    }
    Spacer(GlanceModifier.height(8.dp))

    Row(verticalAlignment = Alignment.Vertical.Bottom) {
        Text(
            text = state.amount.toMoneyText(),
            style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = accent)
        )
        state.tomorrowAmount?.let { tomorrow ->
            Spacer(GlanceModifier.width(10.dp))
            Text(
                text = "내일 ${tomorrow.toMoneyText()}",
                style = TextStyle(fontSize = 12.sp, color = subText)
            )
        }
    }

    Spacer(GlanceModifier.height(10.dp))
    LinearProgressIndicator(
        progress = state.progress,
        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        color = accent,
        backgroundColor = track
    )
    Spacer(GlanceModifier.height(8.dp))

    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = state.spentText,
            style = TextStyle(fontSize = 11.sp, color = subText),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = "${state.remainingDays}일 남음",
            style = TextStyle(fontSize = 11.sp, color = subText)
        )
    }
}
