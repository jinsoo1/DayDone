package com.jsworld.android.daydone.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DayDoneWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = DayDoneWidget
}

/**
 * 위젯 갱신 진입점.
 *
 * 지출·수익·예산·예정 차감이 바뀌면 즉시 부른다 — 지출을 넣었는데 위젯이 그대로면
 * 신뢰가 깨지므로 이 경로가 가장 중요하다. 자정 넘김은 updatePeriodMillis 가 받쳐준다.
 */
suspend fun refreshDayDoneWidget(context: Context) {
    runCatching {
        DayDoneWidget.updateAll(context)
    }.onFailure {
        // 위젯 갱신 실패가 앱을 죽이면 안 되지만, 조용히 사라져도 안 된다
        Log.w(WIDGET_LOG, "위젯 갱신 실패", it)
    }
}

internal const val WIDGET_LOG = "DayDoneWidget"
