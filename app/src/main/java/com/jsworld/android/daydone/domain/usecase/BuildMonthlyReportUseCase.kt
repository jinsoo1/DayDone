package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.DeductionShare
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseCategory
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.MonthlyReport
import com.jsworld.android.daydone.domain.model.PreviousComparison
import com.jsworld.android.daydone.domain.model.ReportCategory
import com.jsworld.android.daydone.domain.model.ReportItem
import com.jsworld.android.daydone.domain.model.ReportPace
import com.jsworld.android.daydone.domain.model.ReportSuggestion
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 진행 중 기간 리포트 계산 (모두 파생, 저장 없음).
 * - 페이스: 기간 경과율 vs 생활비 소진율 비교 (±7%p 안이면 ON_TRACK)
 * - 소진율 분모(순수 생활비) = 예산+추가수익 − 저축 − 고정비, 분자 = 모든 지출(준비금 포함)
 * - 카테고리/하루평균/무지출/필수 비중은 일반 지출(GENERAL)만 본다
 */
class BuildMonthlyReportUseCase @Inject constructor(
    private val classifyExpenseCategoryUseCase: ClassifyExpenseCategoryUseCase
) {

    operator fun invoke(
        period: BudgetPeriod,
        today: LocalDate,
        totalAvailableBudget: Long,   // 월 예산 + 추가 수익
        deductions: List<ScheduledDeduction>,  // 이번 기간 유효 금액으로 resolve된 목록
        expenses: List<Expense>,
        firstRecordDate: LocalDate? = null, // 전체 기록 중 최초 지출 날짜 — 그 전 날들은 무지출로 세지 않음
        firstUseDate: LocalDate? = null,    // 가입일 — "이전 지출 한 줄" 유저 구분에만 사용
        previousPeriod: BudgetPeriod? = null,           // 지난 기간 (비교용)
        previousExpenses: List<Expense> = emptyList()   // 지난 기간의 지출
    ): MonthlyReport {
        val savingTotal = deductions
            .filter { it.type == ScheduledDeductionType.SAVING }
            .sumOf { it.amount }
        val fixedTotal = deductions
            .filter { it.type == ScheduledDeductionType.FIXED }
            .sumOf { it.amount }
        // 기간이 이미 끝났으면 결산 모드 (마지막 날 기준으로 계산)
        val isFinal = today.isAfter(period.endDate)
        val effectiveToday = if (isFinal) period.endDate else today

        val totalDays = (ChronoUnit.DAYS.between(period.startDate, period.endDate).toInt() + 1)
            .coerceAtLeast(1)
        val dayIndex = (ChronoUnit.DAYS.between(period.startDate, effectiveToday).toInt() + 1)
            .coerceIn(1, totalDays)
        val remainingDays = if (isFinal) 0 else (totalDays - dayIndex + 1).coerceAtLeast(1)

        val pureBudget = totalAvailableBudget - savingTotal - fixedTotal
        val totalSpent = expenses.sumOf { it.amount }

        val elapsedPercent = dayIndex * 100 / totalDays
        // 초과 시 절삭으로 100%에 머물지 않도록 올림 처리 (100.2% → 101%)
        val spentPercent = if (pureBudget > 0L) {
            ((totalSpent * 100 + pureBudget - 1) / pureBudget).toInt()
        } else {
            100
        }

        // 초과 판정은 퍼센트 절삭 오차 없이 실제 금액으로
        val pace = when {
            pureBudget > 0L && totalSpent * 100 >= pureBudget * 120 -> ReportPace.WAY_OVER
            totalSpent > pureBudget -> ReportPace.OVER
            elapsedPercent - spentPercent >= 7 -> ReportPace.GOOD
            spentPercent - elapsedPercent > 7 -> ReportPace.FAST
            else -> ReportPace.ON_TRACK
        }

        val general = expenses.filter { it.type == ExpenseType.GENERAL }
        val generalTotal = general.sumOf { it.amount }

        val dailyAverage = generalTotal / dayIndex

        // 무지출: 시작일부터 어제까지(결산이면 기간 끝까지), 일반 지출이 없는 날.
        // 기록 시작(최초 지출) 전 날들은 기록 자체가 없어 가짜 무지출로 부풀므로
        // 기록 시작일부터 센다. 기간 중간부터면 trackingStartDate 로 노출해 안내 문구를 띄운다.
        val spentDates = general.map { it.date }.toSet()
        var noSpendDays = 0
        // "가입 전 지출"을 기간 시작일에 한 줄로 넣은 유저: 가입일 전 지출 날짜가
        // 기간 시작일 하나뿐인 패턴 → 실사용 시작은 가입일. 그 한 줄 때문에
        // 가입 전 날들이 가짜 무지출로 세어지는 것을 막는다. (날짜별 기록이
        // 여러 날 있으면 실제로 기록해온 것이므로 최초 지출부터 그대로 센다.)
        val trackingBase = if (
            firstUseDate != null && firstRecordDate != null &&
            firstRecordDate.isBefore(firstUseDate) &&
            spentDates.filter { it.isBefore(firstUseDate) }.toSet() == setOf(period.startDate)
        ) {
            firstUseDate
        } else {
            firstRecordDate
        }
        val trackingStartDate = trackingBase
            ?.takeIf { it.isAfter(period.startDate) && !it.isAfter(period.endDate) }
        var cursor = trackingStartDate ?: period.startDate
        val lastConfirmed = if (isFinal) period.endDate else today.minusDays(1)
        while (!cursor.isAfter(lastConfirmed) && !cursor.isAfter(period.endDate)) {
            if (cursor !in spentDates) noSpendDays++
            cursor = cursor.plusDays(1)
        }

        // 지난 기간 대비 — 전부 "같은 시점(dayIndex)까지"끼리 비교해야 진행 중 왜곡이 없다.
        // 지난 기간이 처음부터 온전히 기록된 경우에만 계산(부분 기록과의 비교는 어긋난 비교).
        val previous = if (
            previousPeriod != null &&
            firstRecordDate != null &&
            !firstRecordDate.isAfter(previousPeriod.startDate)
        ) {
            val prevCutoff = minOf(
                previousPeriod.startDate.plusDays((dayIndex - 1).toLong()),
                previousPeriod.endDate
            )
            val prevUpToNow = previousExpenses.filter { !it.date.isAfter(prevCutoff) }
            val prevGeneral = prevUpToNow.filter { it.type == ExpenseType.GENERAL }
            val prevSpentDates = prevGeneral.map { it.date }.toSet()

            // 이번 기간과 같은 확정 일수만큼만 무지출을 센다 (진행 중=어제까지 dayIndex-1일, 결산=dayIndex일)
            val confirmedDays = if (isFinal) dayIndex else dayIndex - 1
            var prevNoSpend = 0
            if (confirmedDays >= 1) {
                val prevLastConfirmed = minOf(
                    previousPeriod.startDate.plusDays((confirmedDays - 1).toLong()),
                    previousPeriod.endDate
                )
                var prevCursor = previousPeriod.startDate
                while (!prevCursor.isAfter(prevLastConfirmed)) {
                    if (prevCursor !in prevSpentDates) prevNoSpend++
                    prevCursor = prevCursor.plusDays(1)
                }
            }

            PreviousComparison(
                spentDiff = totalSpent - prevUpToNow.sumOf { it.amount },
                prevDailyAverage = prevGeneral.sumOf { it.amount } / dayIndex,
                prevNoSpendDays = prevNoSpend
            )
        } else {
            null
        }

        val essentialTotal = general.filter { it.isEssential }.sumOf { it.amount }
        val essentialPercent = if (generalTotal > 0L) {
            (essentialTotal * 100 / generalTotal).toInt()
        } else {
            0
        }

        // 카테고리 묶음 + 그 안의 같은 이름 합산
        val categories = general
            .groupBy { classifyExpenseCategoryUseCase(it.title) }
            .map { (category, list) ->
                val items = list
                    .groupBy { it.title.trim() }
                    .map { (title, sameTitle) ->
                        ReportItem(
                            title = title,
                            count = sameTitle.size,
                            total = sameTitle.sumOf { it.amount }
                        )
                    }
                    .sortedByDescending { it.total }

                ReportCategory(
                    category = category,
                    count = list.size,
                    total = list.sumOf { it.amount },
                    items = items
                )
            }
            .sortedByDescending { it.total }

        // 남은 기간 전망 (결산이면 실제 남은 돈이 곧 결과)
        val remaining = pureBudget - totalSpent
        val projectedLeftover = if (isFinal) remaining else remaining - dailyAverage * remainingDays
        val recommendedDaily = if (isFinal || remaining <= 0L) 0L else remaining / remainingDays

        val deductionPercent = if (totalAvailableBudget > 0L) {
            ((savingTotal + fixedTotal) * 100 / totalAvailableBudget).toInt()
        } else {
            0
        }

        // --- 고정지출 상세 분석 ---

        val savingPercent = if (totalAvailableBudget > 0L) {
            (savingTotal * 100 / totalAvailableBudget).toInt()
        } else {
            0
        }

        val deductionShares = deductions
            .map { deduction ->
                DeductionShare(
                    title = deduction.title,
                    amount = deduction.amount,
                    type = deduction.type,
                    percentOfIncome = if (totalAvailableBudget > 0L) {
                        (deduction.amount * 100 / totalAvailableBudget).toInt()
                    } else {
                        0
                    }
                )
            }
            .sortedByDescending { it.amount }

        val suggestions = buildSuggestions(
            savingTotal = savingTotal,
            savingPercent = savingPercent,
            fixedTotal = fixedTotal,
            deductionPercent = deductionPercent,
            deductions = deductions,
            categories = categories,
            general = general,
            dayIndex = dayIndex,
            noSpendDays = noSpendDays,
            essentialPercent = essentialPercent,
            dailyAverage = dailyAverage,
            projectedLeftover = projectedLeftover,
            pace = pace,
            overAmount = (totalSpent - pureBudget).coerceAtLeast(0L)
        )

        return MonthlyReport(
            isFinal = isFinal,
            periodText = "${period.startDate} ~ ${period.endDate}",
            dayIndex = dayIndex,
            totalDays = totalDays,
            remainingDays = remainingDays,
            elapsedPercent = elapsedPercent,
            spentPercent = spentPercent.coerceIn(0, 999),
            pace = pace,
            dailyAverage = dailyAverage,
            noSpendDays = noSpendDays,
            trackingStartDate = trackingStartDate,
            previous = previous,
            essentialPercent = essentialPercent,
            categories = categories,
            projectedLeftover = projectedLeftover,
            recommendedDaily = recommendedDaily,
            savingTotal = savingTotal,
            fixedTotal = fixedTotal,
            deductionPercent = deductionPercent,
            savingPercent = savingPercent,
            deductionShares = deductionShares,
            suggestions = suggestions
        )
    }

    /**
     * 데이터에서 파생되는 규칙 기반 맞춤 제안. 항상 1건 이상, 최대 10건.
     * 순서: 돈 구조(저축/고정비) → 소비 패턴 → 칭찬/위로 (§13 톤).
     */
    private fun buildSuggestions(
        savingTotal: Long,
        savingPercent: Int,
        fixedTotal: Long,
        deductionPercent: Int,
        deductions: List<ScheduledDeduction>,
        categories: List<ReportCategory>,
        general: List<Expense>,
        dayIndex: Int,
        noSpendDays: Int,
        essentialPercent: Int,
        dailyAverage: Long,
        projectedLeftover: Long,
        pace: ReportPace,
        overAmount: Long
    ): List<ReportSuggestion> {
        val suggestions = mutableListOf<ReportSuggestion>()
        val generalTotal = general.sumOf { it.amount }

        // ── 예산 초과 (가장 먼저 — 지금 제일 필요한 이야기) ──

        if (overAmount > 0L) {
            val topCategory = categories.firstOrNull()
            suggestions += if (topCategory != null) {
                ReportSuggestion(
                    "🧭",
                    "예산보다 ${formatMoney(overAmount)} 더 썼어요. 가장 비중이 큰 " +
                            "'${topCategory.category.label}'(${formatMoney(topCategory.total)})부터 " +
                            "돌아보면 원인이 보여요."
                )
            } else {
                ReportSuggestion(
                    "🧭",
                    "예산보다 ${formatMoney(overAmount)} 더 썼어요. 지출 내역을 한번 돌아봐요."
                )
            }

            if (pace == ReportPace.WAY_OVER) {
                suggestions += ReportSuggestion(
                    "📐",
                    "매달 비슷하게 넘친다면 예산이 현실보다 작게 잡힌 걸 수도 있어요. " +
                            "월 탭에서 이번 달 예산을 조정할 수 있어요."
                )
            }
        }

        // ── 돈 구조 ──

        // 저축률 평가 (1인 가구 권장 20% 기준)
        suggestions += when {
            savingTotal <= 0L && projectedLeftover > 0L -> ReportSuggestion(
                "💰",
                "저축 항목이 아직 없어요. 이번 기간 남을 것 같은 " +
                        "${formatMoney(projectedLeftover)}으로 첫 저축을 시작해보면 어때요?"
            )
            savingTotal <= 0L -> ReportSuggestion(
                "💰", "저축 항목이 아직 없어요. 5만 원처럼 작게 시작해도 미래의 내가 고마워해요."
            )
            savingPercent >= 30 -> ReportSuggestion(
                "💰", "수입의 ${savingPercent}%를 저축하고 있어요. 아주 훌륭한 비율이에요."
            )
            savingPercent >= 20 -> ReportSuggestion(
                "💰", "저축률 ${savingPercent}% — 1인 가구 권장(20%)을 잘 지키고 있어요."
            )
            else -> ReportSuggestion(
                "💰", "저축률 ${savingPercent}% — 다음 달엔 1~2%만 더 올려 20%에 도전해볼까요?"
            )
        }

        // 고정비 중 가장 큰 항목
        val topFixed = deductions
            .filter { it.type == ScheduledDeductionType.FIXED }
            .maxByOrNull { it.amount }
        if (topFixed != null && fixedTotal > 0L) {
            val share = (topFixed.amount * 100 / fixedTotal).toInt()
            if (share >= 40) {
                suggestions += ReportSuggestion(
                    "🧾",
                    "고정비의 ${share}%가 '${topFixed.title}'이에요. " +
                            "갱신이나 요금제 변경 시점에 한 번 비교해볼 만해요."
                )
            }
        }

        // 일반 지출에 섞여 있는 구독성 지출 → 고정비 등록 제안
        categories.find { it.category == ExpenseCategory.SUBSCRIPTION }?.let { sub ->
            if (sub.total > 0L) {
                suggestions += ReportSuggestion(
                    "🔁",
                    "구독·통신성 지출 ${formatMoney(sub.total)}이 일반 지출에 섞여 있어요. " +
                            "매달 나가는 거라면 저축/고정비로 등록하면 예산이 더 정확해져요."
                )
            }
        }

        // 선차감 비중이 큰 경우
        if (deductionPercent >= 50) {
            suggestions += ReportSuggestion(
                "⚖️",
                "수입의 절반 이상(${deductionPercent}%)이 매달 저축·고정비로 먼저 빠져나가요. " +
                        "당장 줄이라는 뜻은 아니에요 — 갱신 시기가 온 항목부터 가볍게 점검해봐요."
            )
        }

        // ── 소비 패턴 (기간이 어느 정도 쌓였을 때만) ──

        // 요일 집중: 특정 요일에 지출이 몰리는 경우
        if (dayIndex >= 7 && general.size >= 5 && generalTotal > 0L) {
            val byDayOfWeek = general.groupBy { it.date.dayOfWeek }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
            val top = byDayOfWeek.maxByOrNull { it.value }
            if (top != null && top.value * 100 / generalTotal >= 35) {
                suggestions += ReportSuggestion(
                    "📅",
                    "지출이 ${top.key.toKorean()}요일에 가장 몰려요" +
                            "(${formatMoney(top.value)} · 전체의 ${top.value * 100 / generalTotal}%). " +
                            "그날만 미리 계획해도 페이스가 안정돼요."
                )
            }
        }

        // 주말 과속: 주말 하루 평균이 평일의 1.5배 이상
        if (dayIndex >= 7) {
            val weekend = general.filter { it.date.dayOfWeek.value >= 6 }.sumOf { it.amount }
            val weekday = general.filter { it.date.dayOfWeek.value < 6 }.sumOf { it.amount }
            val weekendDays = general.map { it.date }.distinct().count { it.dayOfWeek.value >= 6 }
                .coerceAtLeast(1)
            val weekdayDays = general.map { it.date }.distinct().count { it.dayOfWeek.value < 6 }
                .coerceAtLeast(1)
            val weekendAvg = weekend / weekendDays
            val weekdayAvg = weekday / weekdayDays
            if (weekdayAvg > 0L && weekendAvg >= weekdayAvg * 3 / 2) {
                suggestions += ReportSuggestion(
                    "🌤️",
                    "주말 하루 평균(${formatMoney(weekendAvg)})이 평일(${formatMoney(weekdayAvg)})보다 " +
                            "확 커요. 주말 계획만 세워도 한 달이 편해져요."
                )
            }
        }

        // 소액 다건 습관
        val smallOnes = general.filter { it.amount in 1..9_999 }
        if (smallOnes.size >= 8) {
            suggestions += ReportSuggestion(
                "🪙",
                "1만 원 아래 지출이 ${smallOnes.size}번, 합치면 " +
                        "${formatMoney(smallOnes.sumOf { it.amount })}이에요. " +
                        "잔잔한 지출도 모이면 하루 권장 금액을 훌쩍 넘죠."
            )
        }

        // 카페 빈도: 평균 이틀에 한 번 이상
        categories.find { it.category == ExpenseCategory.CAFE }?.let { cafe ->
            if (dayIndex >= 6 && cafe.count * 2 >= dayIndex) {
                suggestions += ReportSuggestion(
                    "☕",
                    "카페·간식을 평균 이틀에 한 번 이상 들렀어요(${cafe.count}회 · " +
                            "${formatMoney(cafe.total)}). 횟수를 반만 줄여도 " +
                            "${formatMoney(cafe.total / 2)}이 남아요."
                )
            }
        }

        // 배달 빈도
        val deliveryKeywords = listOf("배달", "배민", "요기요", "쿠팡이츠")
        val delivery = general.filter { expense ->
            val t = expense.title.replace(" ", "")
            deliveryKeywords.any { t.contains(it) }
        }
        if (delivery.size >= 3) {
            suggestions += ReportSuggestion(
                "🛵",
                "배달 주문이 ${delivery.size}회(${formatMoney(delivery.sumOf { it.amount })})예요. " +
                        "한 번만 포장으로 바꿔도 배달비만큼 여유가 생겨요."
            )
        }

        // 가장 큰 하루 (하루 평균의 2배 이상)
        if (dayIndex >= 5 && dailyAverage > 0L) {
            val biggestDay = general.groupBy { it.date }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .maxByOrNull { it.value }
            if (biggestDay != null && biggestDay.value >= dailyAverage * 2) {
                suggestions += ReportSuggestion(
                    "📌",
                    "가장 큰 하루는 ${biggestDay.key.monthValue}월 ${biggestDay.key.dayOfMonth}일" +
                            "(${formatMoney(biggestDay.value)})이었어요. 큰 지출 다음 날을 " +
                            "무지출 데이로 삼으면 페이스가 금방 돌아와요."
                )
            }
        }

        // ── 칭찬 / 위로 ──

        if (noSpendDays >= 3) {
            suggestions += ReportSuggestion(
                "🎉",
                "벌써 무지출 ${noSpendDays}일! 지갑이 쉬는 날이 쌓이고 있어요."
            )
        }

        if (essentialPercent >= 60 && generalTotal > 0L) {
            suggestions += ReportSuggestion(
                "🧷",
                "지출의 ${essentialPercent}%가 필수 지출이었어요. 줄이기 어려운 지출이 많았던 " +
                        "기간이니, 스스로를 탓하지 않아도 돼요."
            )
        }

        return suggestions.take(10)
    }

    private fun formatMoney(amount: Long): String =
        "%,d원".format(amount)

    private fun java.time.DayOfWeek.toKorean(): String = when (this) {
        java.time.DayOfWeek.MONDAY -> "월"
        java.time.DayOfWeek.TUESDAY -> "화"
        java.time.DayOfWeek.WEDNESDAY -> "수"
        java.time.DayOfWeek.THURSDAY -> "목"
        java.time.DayOfWeek.FRIDAY -> "금"
        java.time.DayOfWeek.SATURDAY -> "토"
        java.time.DayOfWeek.SUNDAY -> "일"
    }
}
