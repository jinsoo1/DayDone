package com.jsworld.android.daydone.presentation.report

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.ReportSuggestion
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.presentation.util.toWeekText

/**
 * [ReportSuggestion] → 실제 문장.
 *
 * UseCase 는 어떤 제안을 할지만 정하고(규칙 13종), 문장은 여기서 만든다(v1.5 국제화 3-4).
 * 규칙을 바꾸는 건 UseCase, 말투를 다듬는 건 `strings.xml` — 손댈 곳이 갈린다.
 */
@Composable
fun ReportSuggestion.text(): String = when (this) {
    is ReportSuggestion.OverBudget ->
        if (topCategory != null) {
            stringResource(
                R.string.report_advice_over_budget_top,
                overAmount.toMoneyText(),
                topCategory.label,
                topCategoryTotal.toMoneyText()
            )
        } else {
            stringResource(R.string.report_advice_over_budget, overAmount.toMoneyText())
        }

    ReportSuggestion.BudgetMayBeTooSmall ->
        stringResource(R.string.report_advice_budget_too_small)

    is ReportSuggestion.NoSavingWithLeftover -> stringResource(
        R.string.report_advice_no_saving_with_leftover,
        projectedLeftover.toMoneyText()
    )

    ReportSuggestion.NoSaving -> stringResource(R.string.report_advice_no_saving)

    is ReportSuggestion.SavingRateExcellent ->
        stringResource(R.string.report_advice_saving_excellent, percent)

    is ReportSuggestion.SavingRateGood ->
        stringResource(R.string.report_advice_saving_good, percent)

    is ReportSuggestion.SavingRateLow ->
        stringResource(R.string.report_advice_saving_low, percent)

    is ReportSuggestion.TopFixedShare ->
        stringResource(R.string.report_advice_top_fixed, percentOfFixed, title)

    is ReportSuggestion.SubscriptionInGeneral ->
        stringResource(R.string.report_advice_subscription, total.toMoneyText())

    is ReportSuggestion.DeductionHeavy ->
        stringResource(R.string.report_advice_deduction_heavy, percentOfIncome)

    is ReportSuggestion.DayOfWeekConcentration -> stringResource(
        R.string.report_advice_day_of_week,
        dayOfWeek.toWeekText(),
        amount.toMoneyText(),
        percent
    )

    is ReportSuggestion.WeekendSpending -> stringResource(
        R.string.report_advice_weekend,
        weekendAverage.toMoneyText(),
        weekdayAverage.toMoneyText()
    )

    is ReportSuggestion.ManySmallSpends ->
        stringResource(R.string.report_advice_small_spends, count, total.toMoneyText())

    is ReportSuggestion.CafeFrequent -> stringResource(
        R.string.report_advice_cafe,
        count,
        total.toMoneyText(),
        halfTotal.toMoneyText()
    )

    is ReportSuggestion.DeliveryFrequent ->
        stringResource(R.string.report_advice_delivery, count, total.toMoneyText())

    is ReportSuggestion.BiggestDay -> stringResource(
        R.string.report_advice_biggest_day,
        date.monthValue,
        date.dayOfMonth,
        amount.toMoneyText()
    )

    is ReportSuggestion.NoSpendPraise ->
        stringResource(R.string.report_advice_no_spend_praise, days)

    is ReportSuggestion.EssentialHeavy ->
        stringResource(R.string.report_advice_essential_heavy, percent)
}
