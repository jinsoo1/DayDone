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

/** 상세 분석의 맞춤 제안 한 건. */
data class ReportSuggestion(
    val emoji: String,
    val text: String
)

/** 고정지출 상세: 항목별 수입 대비 비중. */
data class DeductionShare(
    val title: String,
    val amount: Long,
    val type: ScheduledDeductionType,
    val percentOfIncome: Int
)

/** 기간 리포트 (모든 값은 계산 시점 파생). 진행 중/결산 겸용. */
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
