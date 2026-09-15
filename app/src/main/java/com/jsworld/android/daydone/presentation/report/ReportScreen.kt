package com.jsworld.android.daydone.presentation.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.MonthlyReport
import com.jsworld.android.daydone.domain.model.ReportCategory
import com.jsworld.android.daydone.domain.model.ReportPace
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import com.jsworld.android.daydone.ui.theme.DayDoneAccent

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
                title = if (uiState.report?.isFinal == true) stringResource(R.string.report_gigan_gyeolsan_ripoteu) else stringResource(R.string.report_ibeon_gigan_ripoteu),
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
        if (report.previous != null) {
            item { PreviousComparisonLine(report) }
        }
        item { MiniStatsSection(report) }
        report.trackingStartDate?.let { start ->
            item {
                Text(
                    text = stringResource(R.string.report_weol_ilbuteo_girogeul_sijaghaesseoyo, start.monthValue, start.dayOfMonth),
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
            ReportPace.GOOD -> stringResource(R.string.report_yesan_aneseo_hulryunghage_bonaesseoyo)
            ReportPace.ON_TRACK -> stringResource(R.string.report_gyehoeghan_peiseudaero_jal_machyeosseoyo)
            ReportPace.FAST -> stringResource(R.string.report_ibeonen_peiseuga_jom_ppalrasseoyo)
            ReportPace.OVER -> stringResource(R.string.report_yesaneul_jogeum_neomgyeosseoyo_daeum)
            ReportPace.WAY_OVER -> stringResource(R.string.report_yesaneul_manhi_neomgin_giganieosseoyo)
        }
    } else {
        when (report.pace) {
            ReportPace.GOOD -> stringResource(R.string.report_yesan_daebi_hulryunghage_sseuneun)
            ReportPace.ON_TRACK -> stringResource(R.string.report_ttag_joheun_peiseuro_gago)
            ReportPace.FAST -> stringResource(R.string.report_peiseuga_jogeum_ppalrayo_gati)
            ReportPace.OVER -> stringResource(R.string.report_yesaneul_neomeosseoyo_jigeumbuteon_kkog)
            ReportPace.WAY_OVER -> stringResource(R.string.report_yesaneul_manhi_neomeosseoyo_arae)
        }
    }

    val subline = if (report.isFinal) {
        stringResource(R.string.report_gigani_kkeutnasseoyo_saenghwalbiui_reul, report.spentPercent)
    } else {
        stringResource(R.string.report_giganeun_jinassgo_saenghwalbineun_sseosseoyo, report.elapsedPercent, report.spentPercent)
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
                    stringResource(R.string.report_gigan_jongryo, report.periodText)
                } else {
                    stringResource(R.string.report_iljjae, report.dayIndex, report.periodText)
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
                    text = stringResource(R.string.report_sseun_don, report.spentPercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.report_oneul, report.elapsedPercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** 지난 기간 같은 시점 대비 지출 비교 한 줄 (§13: 사실만, 덜 썼을 때만 성공 색). */
@Composable
private fun PreviousComparisonLine(report: MonthlyReport) {
    val previous = report.previous ?: return
    val diff = previous.spentDiff
    val (text, color) = when {
        diff < 0 -> {
            val saved = (-diff).toMoneyText()
            if (report.isFinal) {
                stringResource(R.string.report_jinan_giganboda_deol_sseosseoyo, saved) to DayDoneAccent.successText
            } else {
                stringResource(R.string.report_jinan_gigan_imamttaeboda_deol, saved) to DayDoneAccent.successText
            }
        }
        diff > 0 -> {
            val more = diff.toMoneyText()
            if (report.isFinal) {
                stringResource(R.string.report_jinan_giganboda_deo_sseosseoyo, more) to MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                stringResource(R.string.report_jinan_gigan_imamttaeboda_deo, more) to MaterialTheme.colorScheme.onSurfaceVariant
            }
        }
        else ->
            stringResource(R.string.report_jinan_gigangwa_biseushage_sseugo) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = color
    )
}

@Composable
private fun MiniStatsSection(report: MonthlyReport) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            // 보조 줄("지난 기간 이맘때 …")이 있는 카드와 없는 카드가 섞이고 줄바꿈도 달라서
            // 높이를 가장 높은 카드에 맞춘다 (IntrinsicSize.Min + fillMaxHeight).
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val previousLabel = if (report.isFinal) stringResource(R.string.report_jinan_gigan) else stringResource(R.string.report_jinan_gigan_imamttae)

            MiniStat(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                stringResource(R.string.report_haru_pyeonggyun), report.dailyAverage.toMoneyText(),
                sub = report.previous?.let {
                    "$previousLabel ${it.prevDailyAverage.toMoneyText()}"
                }
            )
            MiniStat(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                stringResource(R.string.report_mujichul), stringResource(R.string.report_il, report.noSpendDays),
                sub = report.previous?.let { stringResource(R.string.report_il_2, previousLabel, it.prevNoSpendDays) }
            )
            MiniStat(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                stringResource(R.string.report_pilsu_bijung), "${report.essentialPercent}%"
            )
        }

        // 보조 줄이 "지난 기간 전체"가 아니라 "같은 시점까지"라는 걸 한 번 설명해준다.
        report.previous?.let {
            Text(
                text = if (report.isFinal) {
                    stringResource(R.string.report_kadeu_arae_jinan_gigan)
                } else {
                    stringResource(R.string.report_kadeu_arae_jinan_gigan_2, report.dayIndex)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniStat(modifier: Modifier, label: String, value: String, sub: String? = null) {
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
        sub?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun CategoryCard(report: MonthlyReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.report_jaju_sseun_gos), style = MaterialTheme.typography.titleMedium)

            if (report.categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.report_ajig_ibeon_gigan_jichuli),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxTotal = report.categories.first().total.coerceAtLeast(1L)
                report.categories.take(5).forEach { category ->
                    CategoryRow(category = category, maxTotal = maxTotal)
                }

                Text(
                    text = stringResource(R.string.report_jichulmyeongeul_bogo_jadongeuro_mukkeosseoyo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 카테고리를 펼쳤을 때 개별로 보여줄 항목 수. 나머지는 "외 N개"로 합친다. */
private const val DETAIL_ITEM_LIMIT = 6

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
                text = stringResource(R.string.report_hoe, category.category.emoji, stringResource(category.category.labelRes), category.count),
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
            category.items.take(DETAIL_ITEM_LIMIT).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.report_hoe_2, item.title, item.count),
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

            // 잘린 항목을 그냥 버리면 펼친 내역의 합이 위 카테고리 금액과 안 맞는다.
            // 건수와 금액을 함께 남겨 숫자가 항상 맞아떨어지게 한다.
            val rest = category.items.drop(DETAIL_ITEM_LIMIT)
            if (rest.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.report_oe_gae, rest.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = rest.sumOf { it.total }.toMoneyText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                text = stringResource(R.string.report_ibeon_gigan_gyeolsan),
                style = MaterialTheme.typography.titleMedium
            )

            if (report.projectedLeftover >= 0L) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.report_saenghwalbieseo))
                        withStyle(
                            SpanStyle(color = greenText, fontWeight = FontWeight.SemiBold)
                        ) {
                            append("+${report.projectedLeftover.toMoneyText()}")
                        }
                        append(stringResource(R.string.report_eul_jikyeonaesseoyo_day_done))
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
                            text = stringResource(R.string.report_namgin_doneun_geumgoui_junbi),
                            style = MaterialTheme.typography.bodySmall,
                            color = greenTintText
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.report_ibeon_giganen_yesanboda_deo, (-report.projectedLeftover).toMoneyText()),
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
                text = stringResource(R.string.report_nameun_il_ireohge_gamyeon, report.remainingDays),
                style = MaterialTheme.typography.titleMedium
            )

            if (report.projectedLeftover >= 0L) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.report_jigeum_peiseumyeon_majimag_nal))
                        withStyle(
                            SpanStyle(color = greenText, fontWeight = FontWeight.SemiBold)
                        ) {
                            append("+${report.projectedLeftover.toMoneyText()}")
                        }
                        append(stringResource(R.string.report_i_namayo_haru))
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(report.recommendedDaily.toMoneyText())
                        }
                        append(stringResource(R.string.report_aneseo_sseumyeon_neogneoghaeyo))
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
                            text = stringResource(R.string.report_namneun_doneun_geumgoui_junbi),
                            style = MaterialTheme.typography.bodySmall,
                            color = greenTintText
                        )
                    }
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.report_jigeum_peiseuga_ieojimyeon_yesaneul))
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(report.recommendedDaily.toMoneyText())
                        }
                        append(stringResource(R.string.report_aneseo_sseumyeon_nameun_nale))
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
            Text(text = stringResource(R.string.report_gojeongjichul_deulyeodabogi), style = MaterialTheme.typography.titleMedium)

            Text(
                text = stringResource(R.string.report_suibui_ga_maedal_jeochug, report.deductionPercent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.report_jeochug), style = MaterialTheme.typography.bodyMedium)
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
                Text(text = stringResource(R.string.report_gojeongbi), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = report.fixedTotal.toMoneyText(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.report_sangse_bunseog),
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
                            text = stringResource(R.string.report_taebhaeseo_sangse_bunseog_yeolgi),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.report_hangmogbyeol_bijung_jeochugryul_pyeongga, report.suggestions.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.report_chulsi_ginyeom_muryo_gonggae),
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
                                    stringResource(R.string.report_jeochug_2)
                                } else {
                                    stringResource(R.string.report_gojeongbi_2)
                                },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.report_suibui, share.amount.toMoneyText(), share.percentOfIncome),
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
                    text = stringResource(R.string.report_majchum_jean),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.report_geon, report.suggestions.size),
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
                        text = suggestion.text(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
