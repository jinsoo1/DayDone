package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ExpenseCategory
import com.jsworld.android.daydone.domain.usecase.category.ExpenseCategoryDictionary
import com.jsworld.android.daydone.presentation.util.AppLocale
import jakarta.inject.Inject
import java.text.Normalizer

/**
 * 지출명 → 카테고리 자동 분류 (내장 키워드 사전).
 *
 * - 입력 마찰 0: 유저는 지출명만 적고, 분류는 리포트 계산 시점에 파생된다.
 * - 매칭: NFKC 정규화·소문자·공백 제거 후 "포함" 검사. **가장 긴(=가장 구체적인) 키워드가 승리**
 *   — "아이스크림"(카페)이 "크림"(뷰티)에 가로채이지 않는다. 길이가 같으면
 *   선언 순서(구체적 카테고리 먼저)가 우선 — "생일 선물"은 경조사.
 * - 어떤 키워드에도 안 걸리면 ETC(기타).
 * - 사전은 로케일별([ExpenseCategoryDictionary]). 규칙은 언어 무관이라 여기 남는다.
 */
class ClassifyExpenseCategoryUseCase internal constructor(
    private val dictionaryProvider: () -> ExpenseCategoryDictionary
) {
    /** 호출 때마다 고른다 — 앱을 켜둔 채 단말 언어를 바꿔도 따라오게. */
    @Inject
    constructor() : this({ ExpenseCategoryDictionary.forLocale(AppLocale.current) })

    /** 테스트·특정 언어 고정용. */
    internal constructor(dictionary: ExpenseCategoryDictionary) : this({ dictionary })

    val dictionary: ExpenseCategoryDictionary
        get() = dictionaryProvider()

    operator fun invoke(title: String): ExpenseCategory {
        val normalized = normalize(title)
        if (normalized.isBlank()) return ExpenseCategory.ETC

        val dict = dictionary

        // 한 글자 키워드는 포함 검사 시 오탐이 커서(미술관→술, 백팩→팩) 정확 일치만 허용
        dict.exactOnly[normalized]?.let { return it }

        var best: ExpenseCategory = ExpenseCategory.ETC
        var bestLength = 0

        // 선언 순서로 돌면서 "더 긴 키워드"일 때만 교체 → 동률이면 앞 순서 유지
        for ((category, keywords) in dict.dictionary) {
            for (keyword in keywords) {
                if (keyword.length > bestLength && normalized.contains(keyword)) {
                    best = category
                    bestLength = keyword.length
                }
            }
        }
        return best
    }

    /** 리포트 "배달 빈도"가 같은 정규화를 쓰도록 노출. */
    fun isDelivery(title: String): Boolean {
        val normalized = normalize(title)
        return dictionary.deliveryKeywords.any { normalized.contains(it) }
    }

    companion object {
        /**
         * NFKC: 전각 영숫자(ＵＮＩＱＬＯ)·반각 카타카나(ｽﾀﾊﾞ)를 표준형으로. 일본어 입력은
         * 폭이 섞이기 쉽다. 한국어엔 영향 없음.
         */
        fun normalize(title: String): String =
            Normalizer.normalize(title, Normalizer.Form.NFKC).lowercase().replace(" ", "").replace("　", "")
    }
}
