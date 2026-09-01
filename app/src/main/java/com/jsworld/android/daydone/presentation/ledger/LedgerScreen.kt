package com.jsworld.android.daydone.presentation.ledger

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.model.LedgerEntryKind
import com.jsworld.android.daydone.presentation.ledger.model.LedgerDayUiModel
import com.jsworld.android.daydone.presentation.ledger.model.LedgerEntryUiModel
import com.jsworld.android.daydone.presentation.ledger.model.LedgerUiState
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import com.jsworld.android.daydone.ui.theme.DayDoneAccent

@Composable
fun LedgerRoute(
    onBack: () -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayDoneTopBar(
                title = uiState.monthTitle.ifBlank { "내역" },
                onBack = onBack
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LedgerContent(
                    uiState = uiState,
                    onToggleSort = viewModel::onToggleSort
                )
            }
        }
    }
}

@Composable
private fun LedgerContent(
    uiState: LedgerUiState,
    onToggleSort: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 40.dp)
    ) {
        item {
            LedgerSummaryCard(uiState = uiState, onToggleSort = onToggleSort)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.days.isEmpty()) {
            item {
                Text(
                    text = "이 기간엔 아직 기록이 없어요.\n지출을 적으면 날짜별로 여기 모여요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            uiState.days.forEach { day ->
                day.monthLabel?.let { label ->
                    item(key = "month-${day.date}") {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                }

                // 날짜는 그 날 항목이 지나갈 때까지 상단에 붙어 있는다 (긴 목록에서 길 잃지 않게)
                stickyHeader(key = "day-${day.date}") {
                    LedgerDayHeader(day)
                }

                itemsIndexed(
                    items = day.entries,
                    key = { index, _ -> "entry-${day.date}-$index" }
                ) { index, entry ->
                    LedgerEntryRow(
                        entry = entry,
                        isLast = index == day.entries.lastIndex
                    )
                }
            }

            item {
                Text(
                    text = "여기선 보기만 해요. 고치려면 월 탭 캘린더에서 그 날짜를 눌러주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun LedgerSummaryCard(
    uiState: LedgerUiState,
    onToggleSort: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.periodText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = if (uiState.ascending) "오래된 날부터 ↑" else "최근 날부터 ↓",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleSort)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 이 화면에서 제일 먼저 보고 싶은 숫자 = 지금 남은 돈. 나머지는 그 아래 내림 계산.
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (uiState.isCurrentPeriod) "남은 생활비" else "남은 금액",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = uiState.remaining.toSignedMoneyText(),
                style = MaterialTheme.typography.headlineSmall
                    .copy(fontFeatureSettings = TabularNum),
                fontWeight = FontWeight.Bold,
                color = if (uiState.remaining < 0L) {
                    DayDoneAccent.spendText
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BreakdownLine(
                label = "월 예산",
                amountText = uiState.monthlyBudget.toMoneyText(),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (uiState.incomeTotal > 0L) {
                BreakdownLine(
                    label = "들어온 돈",
                    amountText = "+${uiState.incomeTotal.toMoneyText()}",
                    color = DayDoneAccent.successText
                )
            }
            BreakdownLine(
                label = "쓴 돈",
                amountText = "−${uiState.spentTotal.toMoneyText()}",
                color = DayDoneAccent.spendText
            )
            if (uiState.deductedTotal > 0L) {
                BreakdownLine(
                    label = "저축·고정비",
                    amountText = "−${uiState.deductedTotal.toMoneyText()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BreakdownLine(label: String, amountText: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyMedium
                .copy(fontFeatureSettings = TabularNum),
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/** 음수면 앞에 −(빼기 기호)를 붙인다 — 초과를 실패로 쓰지 않되 사실은 그대로. */
private fun Long.toSignedMoneyText(): String =
    if (this < 0L) "−${(-this).toMoneyText()}" else toMoneyText()

@Composable
private fun LedgerDayHeader(day: LedgerDayUiModel) {
    // 하루 = 카드 하나. 헤더가 카드의 윗부분(위 모서리만 둥글게)이고 항목 줄들이 이어 붙는다.
    // ⚠️ 스티키 헤더라 **불투명해야** 한다 — 카드색은 반투명 틴트라서 페이지 배경을 먼저 깔고
    //    그 위에 틴트를 얹는다(그냥 틴트만 주면 아래로 지나가는 항목이 비쳐 보인다).
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .background(
                color = ledgerCardColor(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.dayText,
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = TabularNum),
                fontWeight = FontWeight.Bold,
                color = if (day.isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text = day.weekText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (day.isToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (day.isToday) {
                    Text(
                        text = "오늘",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                if (day.spent > 0L) {
                    Text(
                        text = day.spent.toMoneyText(),
                        style = MaterialTheme.typography.titleMedium
                            .copy(fontFeatureSettings = TabularNum),
                        fontWeight = FontWeight.Bold,
                        color = DayDoneAccent.spendText
                    )
                }
                if (day.income > 0L) {
                    Text(
                        text = "+${day.income.toMoneyText()}",
                        style = MaterialTheme.typography.labelMedium
                            .copy(fontFeatureSettings = TabularNum),
                        fontWeight = FontWeight.SemiBold,
                        color = DayDoneAccent.successText
                    )
                }
                if (day.deducted > 0L) {
                    Text(
                        text = "저축·고정비 ${day.deducted.toMoneyText()}",
                        style = MaterialTheme.typography.labelSmall
                            .copy(fontFeatureSettings = TabularNum),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 붙어 있을 때 항목 줄과 구분되도록 얇은 선
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 14.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun LedgerEntryRow(entry: LedgerEntryUiModel, isLast: Boolean) {
    // 색은 아껴 쓴다 — 대부분이 지출이라 전부 빨강이면 아무것도 도드라지지 않는다.
    // 들어온 돈(초록)·저축·고정비(회색)만 색으로 구분하고 지출은 기본색.
    val amountColor = when (entry.kind) {
        LedgerEntryKind.EXTRA_INCOME -> DayDoneAccent.successText
        LedgerEntryKind.SAVING, LedgerEntryKind.FIXED -> MaterialTheme.colorScheme.onSurfaceVariant
        LedgerEntryKind.EXPENSE, LedgerEntryKind.FUTURE_PREPARE -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ledgerCardColor(),
                shape = if (isLast) {
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                } else {
                    RectangleShape
                }
            )
            .padding(start = 14.dp, end = 14.dp, bottom = if (isLast) 6.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 들어온 돈만 왼쪽에 초록 막대 — 훑을 때 눈에 걸리게
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (entry.kind == LedgerEntryKind.EXTRA_INCOME) {
                            DayDoneAccent.successText
                        } else {
                            Color.Transparent
                        }
                    )
            )

            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            entry.tag?.let { tag ->
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = entry.amountText,
                style = MaterialTheme.typography.bodyMedium
                    .copy(fontFeatureSettings = TabularNum),
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 11.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        }
    }

    // 카드 사이 간격 (배경 밖이라 카드가 끊겨 보인다)
    if (isLast) {
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/** 날짜 카드 배경 — 헤더와 항목 줄이 같은 색이어야 하나의 카드로 보인다. */
@Composable
private fun ledgerCardColor(): Color =
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

/** 자릿수가 흔들리지 않는 숫자(tabular) — 금액 세로줄이 맞아 훑기 쉬워진다. */
private const val TabularNum = "tnum"
