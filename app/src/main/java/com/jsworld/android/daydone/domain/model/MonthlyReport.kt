package com.jsworld.android.daydone.domain.model

/** 예산 진행 페이스 판정. */
enum class ReportPace {
    GOOD,     // 경과율보다 확실히 적게 씀
    ON_TRACK, // 경과율과 비슷
    FAST,     // 경과율보다 빠르지만 아직 예산 안
    OVER,     // 예산 초과 (100% 초과)
    WAY_OVER  // 예산을 크게 초과 (120% 이상)
}

/** 카테고리 안의 세부 항목 (같은 지출명 합산). */
data class ReportItem(
    val title: String,
    val count: Int,
    val total: Long
)

data class ReportCategory(
    val category: ExpenseCategory,
    val count: Int,
    val total: Long,
    val items: List<ReportItem>
)

/**
 * 상세 분석의 맞춤 제안 한 건 — **무엇을 말할지**만 정한다.
 *
 * 문장은 presentation 에서 조립한다(v1.5 국제화 3-4). UseCase 엔 Context 가 없어
 * `stringResource` 를 못 쓰고, `StringProvider` 를 주입하면 순수 JVM 테스트가 깨진다.
 *
 * 이모지는 번역 대상이 아니라 **규칙에 붙은 표식**이라 여기 둔다 — 규칙과 떨어뜨리면
 * presentation 에 매핑표가 하나 더 생긴다.
 */
sealed interface ReportSuggestion {
    val emoji: String

    // ── 예산 초과 ────────────────────────────────────────
    /** 가장 비중이 큰 카테고리를 함께 짚어준다. 카테고리가 없으면 [topCategory] 가 null. */
    data class OverBudget(
        val overAmount: Long,
        val topCategory: ExpenseCategory?,
        val topCategoryTotal: Long
    ) : ReportSuggestion {
        override val emoji = "🧭"
    }

    /** 크게 넘쳤을 때 — 예산 자체가 현실보다 작을 수 있다는 이야기. */
    data object BudgetMayBeTooSmall : ReportSuggestion {
        override val emoji = "📐"
    }

    // ── 돈 구조 ─────────────────────────────────────────
    data class NoSavingWithLeftover(val projectedLeftover: Long) : ReportSuggestion {
        override val emoji = "💰"
    }

    data object NoSaving : ReportSuggestion {
        override val emoji = "💰"
    }

    /** 1인 가구 권장 20% 기준 — 30% 이상 / 20% 이상 / 그 아래. */
    data class SavingRateExcellent(val percent: Int) : ReportSuggestion {
        override val emoji = "💰"
    }

    data class SavingRateGood(val percent: Int) : ReportSuggestion {
        override val emoji = "💰"
    }

    data class SavingRateLow(val percent: Int) : ReportSuggestion {
        override val emoji = "💰"
    }

    data class TopFixedShare(val title: String, val percentOfFixed: Int) : ReportSuggestion {
        override val emoji = "🧾"
    }

    data class SubscriptionInGeneral(val total: Long) : ReportSuggestion {
        override val emoji = "🔁"
    }

    data class DeductionHeavy(val percentOfIncome: Int) : ReportSuggestion {
        override val emoji = "⚖️"
    }

    // ── 소비 패턴 ────────────────────────────────────────
    data class DayOfWeekConcentration(
        val dayOfWeek: java.time.DayOfWeek,
        val amount: Long,
        val percent: Int
    ) : ReportSuggestion {
        override val emoji = "📅"
    }

    data class WeekendSpending(
        val weekendAverage: Long,
        val weekdayAverage: Long
    ) : ReportSuggestion {
        override val emoji = "🌤️"
    }

    data class ManySmallSpends(val count: Int, val total: Long) : ReportSuggestion {
        override val emoji = "🪙"
    }

    /** 횟수를 반만 줄이면 [halfTotal] 이 남는다는 안내까지 포함. */
    data class CafeFrequent(
        val count: Int,
        val total: Long,
        val halfTotal: Long
    ) : ReportSuggestion {
        override val emoji = "☕"
    }

    data class DeliveryFrequent(val count: Int, val total: Long) : ReportSuggestion {
        override val emoji = "🛵"
    }

    data class BiggestDay(
        val date: java.time.LocalDate,
        val amount: Long
    ) : ReportSuggestion {
        override val emoji = "📌"
    }

    // ── 칭찬 / 위로 (§13) ────────────────────────────────
    data class NoSpendPraise(val days: Int) : ReportSuggestion {
        override val emoji = "🎉"
    }

    data class EssentialHeavy(val percent: Int) : ReportSuggestion {
        override val emoji = "🧷"
    }
}

/** 고정지출 상세: 항목별 수입 대비 비중. */
data class DeductionShare(
    val title: String,
    val amount: Long,
    val type: ScheduledDeductionType,
    val percentOfIncome: Int
)

/** 기간 리포트 (모든 값은 계산 시점 파생). 진행 중/결산 겸용. */
/**
 * 지난 기간 대비 비교 — 전부 "같은 시점(dayIndex)까지" 기준.
 * 기간 전체와 비교하면 진행 중엔 항상 "덜 씀"으로 보이는 왜곡이 있어서다.
 */
data class PreviousComparison(
    val spentDiff: Long,        // 이번 총지출 − 지난 기간 같은 시점 총지출 (양수 = 더 씀)
    val prevDailyAverage: Long, // 지난 기간 같은 시점까지의 하루 평균 (일반 지출)
    val prevNoSpendDays: Int    // 지난 기간의 같은 확정 일수 안 무지출
)

data class MonthlyReport(
    val isFinal: Boolean,       // 기간이 끝난 결산 리포트인지
    val periodText: String,
    val dayIndex: Int,          // 오늘이 기간 며칠째 (1-based, 결산이면 totalDays)
    val totalDays: Int,
    val remainingDays: Int,     // 결산이면 0

    val elapsedPercent: Int,    // 기간 경과율 %
    val spentPercent: Int,      // 생활비 소진율 %
    val pace: ReportPace,

    val dailyAverage: Long,     // 하루 평균 일반 지출
    val noSpendDays: Int,       // 일반 지출 없는 날 (지난 날 기준)
    val trackingStartDate: java.time.LocalDate? = null, // 기간 중간부터 기록 시작 시 그 날짜 (안내 문구용)
    val previous: PreviousComparison? = null,           // 지난 기간 대비 (없거나 부분 기록이면 null)
    val essentialPercent: Int,  // 일반 지출 중 필수 비중 %

    val categories: List<ReportCategory>,

    val projectedLeftover: Long,   // 지금 페이스면 마지막 날 남는 돈 (음수 가능)
    val recommendedDaily: Long,    // 남은 기간 하루 권장

    val savingTotal: Long,
    val fixedTotal: Long,
    val deductionPercent: Int,     // 수입(예산+추가수익) 대비 저축+고정비 %

    // --- 고정지출 상세 분석 (리워드 잠금 영역) ---
    val savingPercent: Int,               // 수입 대비 저축률 %
    val deductionShares: List<DeductionShare>,
    val suggestions: List<ReportSuggestion>  // 규칙 기반 제안 (데이터에서 파생)
)
