package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 지출명 → 카테고리 자동 분류. "가장 긴 키워드 승리" 규칙이 깨지면
 * 아이스크림이 뷰티로 빠지는 식의 오분류가 다시 생긴다.
 */
class ClassifyExpenseCategoryUseCaseTest {

    private val classify = ClassifyExpenseCategoryUseCase()

    @Test
    fun `기본 분류`() {
        assertEquals(ExpenseCategory.CAFE, classify("커피"))
        assertEquals(ExpenseCategory.FOOD, classify("점심"))
        assertEquals(ExpenseCategory.TRANSPORT, classify("택시"))
        assertEquals(ExpenseCategory.SUBSCRIPTION, classify("넷플릭스"))
        assertEquals(ExpenseCategory.GROCERY, classify("이마트"))
        assertEquals(ExpenseCategory.PET, classify("강아지 사료"))
    }

    @Test
    fun `긴 키워드가 짧은 키워드를 이긴다`() {
        assertEquals(ExpenseCategory.CAFE, classify("아이스크림"))   // 크림(뷰티) 아님
        assertEquals(ExpenseCategory.CAFE, classify("스타벅스"))     // 벅스(구독) 아님
        assertEquals(ExpenseCategory.FASHION, classify("운동화"))    // 운동(건강) 아님
        assertEquals(ExpenseCategory.FASHION, classify("스커트"))    // 커트(뷰티) 아님
        assertEquals(ExpenseCategory.CAFE, classify("배스킨라빈스")) // 스킨(뷰티) 아님
        assertEquals(ExpenseCategory.FOOD, classify("쌀국수"))       // 쌀(장보기) 아님
        assertEquals(ExpenseCategory.CULTURE, classify("만화카페"))  // 카페 아님
    }

    @Test
    fun `크림은 문맥에 따라 갈린다`() {
        assertEquals(ExpenseCategory.BEAUTY, classify("아이크림"))
        assertEquals(ExpenseCategory.BEAUTY, classify("수분크림"))
        assertEquals(ExpenseCategory.BEAUTY, classify("선크림"))
        assertEquals(ExpenseCategory.CAFE, classify("생크림 케이크"))
        assertEquals(ExpenseCategory.CAFE, classify("슈크림"))
        assertEquals(ExpenseCategory.FOOD, classify("크림파스타"))
    }

    @Test
    fun `한 글자 키워드는 정확히 일치할 때만 잡는다`() {
        assertEquals(ExpenseCategory.ALCOHOL, classify("술"))
        assertEquals(ExpenseCategory.CULTURE, classify("미술관"))  // 술 아님
        assertEquals(ExpenseCategory.BEAUTY, classify("팩"))
        assertEquals(ExpenseCategory.FASHION, classify("백팩"))    // 팩 아님
        assertEquals(ExpenseCategory.BEAUTY, classify("펌"))
        assertEquals(ExpenseCategory.ETC, classify("펌프"))        // 펌 아님
        assertEquals(ExpenseCategory.HEALTH, classify("약"))
        assertEquals(ExpenseCategory.HEALTH, classify("감기약"))
    }

    @Test
    fun `띄어쓰기와 대소문자는 무시한다`() {
        assertEquals(ExpenseCategory.TRANSPORT, classify("전기 충전"))
        assertEquals(ExpenseCategory.TRANSPORT, classify("전기충전"))
        assertEquals(ExpenseCategory.SUBSCRIPTION, classify("NETFLIX"))
        assertEquals(ExpenseCategory.SUBSCRIPTION, classify("Netflix 구독"))
    }

    @Test
    fun `구체적인 카테고리가 먼저 적용된다`() {
        assertEquals(ExpenseCategory.OCCASION, classify("생일 선물"))
        assertEquals(ExpenseCategory.OCCASION, classify("생일 케이크"))
        assertEquals(ExpenseCategory.PET, classify("강아지 간식"))
    }

    @Test
    fun `못 알아보는 지출명은 기타로 모인다`() {
        assertEquals(ExpenseCategory.ETC, classify("ㅇㅇ"))
        assertEquals(ExpenseCategory.ETC, classify(""))
        assertEquals(ExpenseCategory.ETC, classify("   "))
    }
}
