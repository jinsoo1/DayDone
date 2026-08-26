package com.jsworld.android.daydone.domain.usecase

import jakarta.inject.Inject
import java.time.YearMonth

/**
 * 큰 지출이 예상되는 달 안내 문구. 해당 없으면 null.
 *
 * 설·추석은 음력이라 java.time 으로 계산할 수 없어 **표로 둔다**(연 1회 갱신).
 * 파생이 아니라 지식 주입이라 데이터가 필요한 유일한 안내다.
 */
class GetBigSpendMonthNoticeUseCase @Inject constructor() {

    operator fun invoke(month: YearMonth): String? {
        val key = "%04d-%02d".format(month.year, month.monthValue)

        LUNAR_HOLIDAYS[key]?.let { return "이번 달엔 ${it}이 있어요. 큰 지출이 예상되면 금고에 미리 나눠 담아두면 편해요." }

        return when (month.monthValue) {
            5 -> "5월엔 가정의 달 행사가 몰려요. 미리 나눠 담아두면 그 달이 편해져요."
            12 -> "12월엔 연말 모임이 늘어나요. 미리 조금씩 준비해두면 좋아요."
            else -> null
        }
    }

    private companion object {
        /** 설·추석이 든 달 (음력 기준이라 직접 관리) */
        val LUNAR_HOLIDAYS = mapOf(
            "2026-02" to "설날",
            "2026-09" to "추석",
            "2027-02" to "설날",
            "2027-09" to "추석",
            "2028-01" to "설날",
            "2028-10" to "추석",
            "2029-02" to "설날",
            "2029-09" to "추석",
            "2030-02" to "설날",
            "2030-09" to "추석"
        )
    }
}
