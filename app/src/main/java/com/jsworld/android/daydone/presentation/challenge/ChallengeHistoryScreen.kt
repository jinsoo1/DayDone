package com.jsworld.android.daydone.presentation.challenge

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar

@Composable
fun ChallengeHistoryRoute(
    onBack: () -> Unit,
    viewModel: ChallengeHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    ChallengeHistoryScreen(
        uiState = uiState,
        onBack = onBack
    )
}

@Composable
fun ChallengeHistoryScreen(
    uiState: ChallengeHistoryUiState,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayDoneTopBar(title = "무지출 도전 기록", onBack = onBack)

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.records.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 끝난 도전이 없어요.\n챌린지를 끝까지 진행하면 여기에 기록이 쌓여요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 8.dp,
                            end = 20.dp,
                            bottom = 40.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.records, key = { it.startDate.toString() }) { record ->
                            ChallengeRecordCard(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeRecordCard(record: NoSpendChallengeRecord) {
    val modeText = when (record.mode) {
        NoSpendMode.FULL -> "완전 무지출"
        NoSpendMode.ESSENTIAL_ALLOWED -> "필수 지출 허용"
        NoSpendMode.CAP -> "하루 ${record.capAmount.toMoneyText()} 이하"
    }

    val badge = when {
        record.successDays >= record.targetDays -> "🏆"
        record.successDays >= (record.targetDays + 1) / 2 -> "🔥"
        else -> "🌱"
    }

    val fraction = if (record.targetDays > 0) {
        (record.successDays.toFloat() / record.targetDays.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.startDate.monthValue}/${record.startDate.dayOfMonth} ~ " +
                            "${record.endDate.monthValue}/${record.endDate.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = badge,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "${record.targetDays}일 도전 · ${record.successDays}일 성공 · $modeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}
