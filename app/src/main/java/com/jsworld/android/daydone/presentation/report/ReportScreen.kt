package com.jsworld.android.daydone.presentation.report

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.model.MonthlyReport
import com.jsworld.android.daydone.domain.model.ReportCategory
import com.jsworld.android.daydone.domain.model.ReportPace
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.theme.DayDoneAccent
import com.jsworld.android.daydone.ui.component.DayDoneTopBar

@Composable
fun ReportRoute(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayDoneTopBar(
                title = if (uiState.report?.isFinal == true) "기간 결산 리포트" else "이번 기간 리포트",
                onBack = onBack
            )

            val report = uiState.report
            if (uiState.isLoading || report == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ReportContent(report = report)
            }
        }
    }
}

@Composable
private fun ReportContent(report: MonthlyReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PaceCard(report) }
        item { MiniStatsRow(report) }
        report.trackingStartDate?.let { start ->
            item {
                Text(
                    text = "🌱 ${start.monthValue}월 ${start.dayOfMonth}일부터 기록을 시작했어요. " +
                            "그 전 날들은 리포트에 담기지 않아 이번 기간은 실제와 조금 다를 수 있어요. " +
                            "다음 기간부터는 온전하게 보여드릴게요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { CategoryCard(report) }
        item {
            if (report.isFinal) {
                FinalSummaryCard(report)
            } else {
                RemainingGuideCard(report)
            }
        }
        item { DeductionCard(report) }
    }
}

@Composable
private fun PaceCard(report: MonthlyReport) {
    val headline = if (report.isFinal) {
        when (report.pace) {
            ReportPace.GOOD -> "예산 안에서 훌륭하게 보냈어요"
            ReportPace.ON_TRACK -> "계획한 페이스대로 잘 마쳤어요"
            ReportPace.FAST -> "이번엔 페이스가 좀 빨랐어요"
            ReportPace.OVER -> "예산을 조금 넘겼어요. 다음 기간에 다시 잡아봐요"
            ReportPace.WAY_OVER -> "예산을 많이 넘긴 기간이었어요. 원인을 알면 다음은 달라져요"
        }
    } else {
        when (report.pace) {
            ReportPace.GOOD -> "예산 대비 훌륭하게 쓰는 중이에요"
            ReportPace.ON_TRACK -> "딱 좋은 페이스로 가고 있어요"
            ReportPace.FAST -> "페이스가 조금 빨라요. 같이 조절해볼까요"
            ReportPace.OVER -> "예산을 넘었어요. 지금부턴 꼭 필요한 것만 함께 지켜봐요"
            ReportPace.WAY_OVER -> "예산을 많이 넘었어요. 아래 '자주 쓴 곳'에서 원인을 찾아봐요"
        }
    }

    val subline = if (report.isFinal) {
        "기간이 끝났어요 · 생활비의 ${report.spentPercent}%를 썼어요"
    } else {
        "기간은 ${report.elapsedPercent}% 지났고, 생활비는 ${report.spentPercent}% 썼어요"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (report.isFinal) {
                    "기간 종료 · ${report.periodText}"
                } else {
                    "${report.dayIndex}일째 · ${report.periodText}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = subline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )

            // 소진율 바 + 오늘(경과율) 마커 (초과 시 경고색)
            val isOver = report.pace == ReportPace.OVER || report.pace == ReportPace.WAY_OVER
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (report.spentPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isOver) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )

                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val markerOffset = maxWidth * (report.elapsedPercent / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .offset(x = markerOffset - 1.dp, y = (-3).dp)
                            .width(2.dp)
                            .height(14.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "쓴 돈 ${report.spentPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "오늘 ${report.elapsedPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MiniStatsRow(report: MonthlyReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniStat(Modifier.weight(1f), "하루 평균", report.dailyAverage.toMoneyText())
        MiniStat(Modifier.weight(1f), "무지출", "${report.noSpendDays}일")
        MiniStat(Modifier.weight(1f), "필수 비중", "${report.essentialPercent}%")
    }
}

@Composable
private fun MiniStat(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CategoryCard(report: MonthlyReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "자주 쓴 곳", style = MaterialTheme.typography.titleMedium)

            if (report.categories.isEmpty()) {
                Text(
                    text = "아직 이번 기간 지출이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxTotal = report.categories.first().total.coerceAtLeast(1L)
                report.categories.take(5).forEach { category ->
                    CategoryRow(category = category, maxTotal = maxTotal)
                }

                Text(
                    text = "지출명을 보고 자동으로 묶었어요. 못 알아본 항목은 기타로 모여요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(category: ReportCategory, maxTotal: Long) {
    var expanded by rememberSaveable(category.category.name) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${category.category.emoji} ${category.category.label} " +
                        "· ${category.count}회",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = category.total.toMoneyText(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        LinearProgressIndicator(
            progress = { (category.total.toFloat() / maxTotal.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )

        if (expanded) {
            category.items.take(6).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.title} · ${item.count}회",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.total.toMoneyText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FinalSummaryCard(report: MonthlyReport) {
    val greenText = DayDoneAccent.successText
    val greenTintBg = DayDoneAccent.successContainer
    val greenTintText = DayDoneAccent.onSuccessContainer

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "이번 기간 결산",
                style = MaterialTheme.typography.titleMedium
            )

            if (report.projectedLeftover >= 0L) {
                Text(
                    text = buildAnnotatedString {
                        append("생활비에서 ")
                        withStyle(
                            SpanStyle(color = greenText, fontWeight = FontWeight.SemiBold)
                        ) {
                            append("+${report.projectedLeftover.toMoneyText()}")
                        }
                        append("을 지켜냈어요. Day Done! 🎉")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (report.projectedLeftover > 0L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(greenTintBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "남긴 돈은 금고의 준비 항목이나 저축으로 옮겨, 다음 큰 지출을 준비해보세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = greenTintText
                        )
                    }
                }
            } else {
                Text(
                    text = "이번 기간엔 예산보다 " +
                            "${(-report.projectedLeftover).toMoneyText()} 더 썼어요. " +
                            "괜찮아요 — 새 기간은 새 예산으로 다시 시작해요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RemainingGuideCard(report: MonthlyReport) {
    // 목업 팔레트: 초록 강조 + 초록 틴트 팁 박스
    val greenText = DayDoneAccent.successText
    val greenTintBg = DayDoneAccent.successContainer
    val greenTintText = DayDoneAccent.onSuccessContainer

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "남은 ${report.remainingDays}일, 이렇게 가면",
                style = MaterialTheme.typography.titleMedium
            )

            if (report.projectedLeftover >= 0L) {
                Text(
                    text = buildAnnotatedString {
                        append("지금 페이스면 마지막 날 ")
                        withStyle(
                            SpanStyle(color = greenText, fontWeight = FontWeight.SemiBold)
                        ) {
                            append("+${report.projectedLeftover.toMoneyText()}")
                        }
                        append("이 남아요.\n하루 ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(report.recommendedDaily.toMoneyText())
                        }
                        append(" 안에서 쓰면 넉넉해요.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (report.projectedLeftover > 0L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(greenTintBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "남는 돈은 금고의 준비 항목에 미리 옮겨두는 것도 좋아요",
                            style = MaterialTheme.typography.bodySmall,
                            color = greenTintText
                        )
                    }
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        append("지금 페이스가 이어지면 예산을 조금 넘을 수 있어요. 괜찮아요 — 하루 ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(report.recommendedDaily.toMoneyText())
                        }
                        append(" 안에서 쓰면 남은 날에 자연스럽게 맞춰져요.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeductionCard(report: MonthlyReport) {
    // TODO: 애드몹 연동 후 unlocked 해제를 리워드 광고 시청 완료 콜백으로 교체
    var unlocked by rememberSaveable { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "고정지출 들여다보기", style = MaterialTheme.typography.titleMedium)

            Text(
                text = "수입의 ${report.deductionPercent}%가 매달 저축·고정비로 먼저 빠져나가요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "저축", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = report.savingTotal.toMoneyText(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "고정비", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = report.fixedTotal.toMoneyText(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider()

            Text(
                text = "상세 분석",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            if (unlocked) {
                DeductionDetail(report)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { unlocked = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .blur(6.dp)
                            .padding(bottom = 4.dp)
                    ) {
                        DeductionDetail(report)
                    }

                    // 블러 미지원 기기 대비 반투명 덮개 + 잠금 안내
                    Column(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔒", style = MaterialTheme.typography.titleMedium)
                        // TODO: 애드몹 리워드 연동 시 "광고 보고 상세 분석 열기"로 변경
                        //  (탭했더니 예고 없이 광고가 뜨지 않도록 문구를 먼저 바꿀 것)
                        Text(
                            text = "탭해서 상세 분석 열기",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "항목별 비중 · 저축률 평가 · 맞춤 제안 ${report.suggestions.size}건",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "출시 기념 무료 공개 중",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeductionDetail(report: MonthlyReport) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 항목별 수입 대비 비중
        report.deductionShares.forEach { share ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = share.title +
                                if (share.type == com.jsworld.android.daydone.domain.model.ScheduledDeductionType.SAVING) {
                                    " · 저축"
                                } else {
                                    " · 고정비"
                                },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${share.amount.toMoneyText()} · 수입의 ${share.percentOfIncome}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { (share.percentOfIncome / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }

        if (report.suggestions.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "맞춤 제안",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${report.suggestions.size}건",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            report.suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = suggestion.emoji,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = suggestion.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
