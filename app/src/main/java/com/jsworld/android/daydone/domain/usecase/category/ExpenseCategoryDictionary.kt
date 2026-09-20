package com.jsworld.android.daydone.domain.usecase.category

import com.jsworld.android.daydone.domain.model.ExpenseCategory
import java.util.Locale

/**
 * 지출명 → 카테고리 키워드 사전. **언어마다 재작성**한다(번역이 아니다 — 브랜드·음식·
 * 서비스명이 나라마다 통째로 다르다). 매칭 규칙(가장 긴 키워드 승리·EXACT_ONLY)은
 * 언어 무관이라 [ClassifyExpenseCategoryUseCase][com.jsworld.android.daydone.domain.usecase.ClassifyExpenseCategoryUseCase] 에 남는다.
 *
 * 키워드 규칙: **공백 없이·영문 소문자로** 등록. 매칭 전에 입력을 NFKC 정규화하므로
 * 전각 영숫자·반각 카타카나는 신경 쓰지 않아도 된다.
 */
interface ExpenseCategoryDictionary {
    /** 한 글자처럼 포함 검사 시 오탐이 큰 키워드 — 정확 일치만 허용. */
    val exactOnly: Map<String, ExpenseCategory>

    /** 선언 순서가 우선순위. 구체적 카테고리 → 일반적 카테고리 순. */
    val dictionary: List<Pair<ExpenseCategory, List<String>>>

    /** 리포트 "배달 빈도" 제안이 보는 키워드. 사전과 같은 지식 주입이라 여기 둔다. */
    val deliveryKeywords: List<String>

    companion object {
        fun forLocale(locale: Locale): ExpenseCategoryDictionary =
            if (locale.language == "ja") JapaneseExpenseCategoryDictionary else KoreanExpenseCategoryDictionary
    }
}
