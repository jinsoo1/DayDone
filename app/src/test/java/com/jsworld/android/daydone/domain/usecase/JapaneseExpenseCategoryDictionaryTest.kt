package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ExpenseCategory
import com.jsworld.android.daydone.domain.usecase.category.ExpenseCategoryDictionary
import com.jsworld.android.daydone.domain.usecase.category.JapaneseExpenseCategoryDictionary
import com.jsworld.android.daydone.domain.usecase.category.KoreanExpenseCategoryDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * 일본어 사전. 규칙은 한국어와 같고(가장 긴 키워드 승리·EXACT_ONLY) 데이터만 다르다.
 * 한국어 테스트가 지키는 것과 같은 종류의 함정(짧은 키워드 가로채기)을 일본어에서 다시 고정한다.
 */
class JapaneseExpenseCategoryDictionaryTest {

    private val classify = ClassifyExpenseCategoryUseCase(JapaneseExpenseCategoryDictionary)

    @Test
    fun `기본 분류 - 일본 유저가 실제로 적는 말`() {
        assertEquals(ExpenseCategory.CAFE, classify("スタバ"))
        assertEquals(ExpenseCategory.FOOD, classify("ランチ"))
        assertEquals(ExpenseCategory.FOOD, classify("サイゼ"))
        assertEquals(ExpenseCategory.FOOD, classify("マック"))
        assertEquals(ExpenseCategory.GROCERY, classify("セブン"))
        assertEquals(ExpenseCategory.GROCERY, classify("業スー"))
        assertEquals(ExpenseCategory.TRANSPORT, classify("Suicaチャージ"))
        assertEquals(ExpenseCategory.TRANSPORT, classify("定期代"))
        assertEquals(ExpenseCategory.SUBSCRIPTION, classify("ネトフリ"))
        assertEquals(ExpenseCategory.SUBSCRIPTION, classify("スマホ代"))
        assertEquals(ExpenseCategory.HEALTH, classify("マツキヨ"))
        assertEquals(ExpenseCategory.BEAUTY, classify("美容院"))
        assertEquals(ExpenseCategory.ALCOHOL, classify("飲み会"))
        assertEquals(ExpenseCategory.ALCOHOL, classify("鳥貴族"))
        assertEquals(ExpenseCategory.FASHION, classify("ユニクロ"))
        assertEquals(ExpenseCategory.LIVING, classify("家賃"))
        assertEquals(ExpenseCategory.LIVING, classify("ダイソー"))
        assertEquals(ExpenseCategory.CULTURE, classify("映画"))
        assertEquals(ExpenseCategory.PET, classify("猫砂"))
        assertEquals(ExpenseCategory.OCCASION, classify("ご祝儀"))
        assertEquals(ExpenseCategory.TRAVEL, classify("じゃらん"))
        assertEquals(ExpenseCategory.EDUCATION, classify("英会話"))
    }

    @Test
    fun `긴 키워드가 짧은 키워드를 이긴다`() {
        assertEquals(ExpenseCategory.CAFE, classify("アイスクリーム"))     // クリーム(뷰티) 아님
        assertEquals(ExpenseCategory.BEAUTY, classify("アイクリーム"))
        assertEquals(ExpenseCategory.FASHION, classify("パンツ"))          // パン(카페) 아님
        assertEquals(ExpenseCategory.FASHION, classify("スーツ"))
        assertEquals(ExpenseCategory.TRAVEL, classify("スーツケース"))     // ケース(생활)·スーツ(패션) 아님
        assertEquals(ExpenseCategory.FOOD, classify("フードコート"))       // コート(패션) 아님
        assertEquals(ExpenseCategory.LIVING, classify("炊飯器"))           // 飯(식비) 아님
        assertEquals(ExpenseCategory.FOOD, classify("油そば"))             // 油(식료품) 아님
        assertEquals(ExpenseCategory.CULTURE, classify("漫画喫茶"))        // 茶(카페) 아님
        assertEquals(ExpenseCategory.TRANSPORT, classify("エンジンオイル"))
        assertEquals(ExpenseCategory.LIVING, classify("フライパン"))       // パン(카페) 아님
        assertEquals(ExpenseCategory.GROCERY, classify("醤油"))
    }

    @Test
    fun `한 글자 한자는 정확 일치만`() {
        assertEquals(ExpenseCategory.ALCOHOL, classify("酒"))
        assertEquals(ExpenseCategory.CULTURE, classify("本"))
        assertEquals(ExpenseCategory.ETC, classify("日本"))     // 本(문화)에 안 걸림
        assertEquals(ExpenseCategory.ETC, classify("服部さん"))  // 服(패션)에 안 걸림
        assertEquals(ExpenseCategory.PET, classify("犬"))
        assertEquals(ExpenseCategory.GROCERY, classify("米"))
        assertEquals(ExpenseCategory.GROCERY, classify("米5kg"))
    }

    @Test
    fun `전각·반각·대소문자를 가리지 않는다`() {
        assertEquals(ExpenseCategory.FASHION, classify("ＵＮＩＱＬＯ"))   // 전각 영문
        assertEquals(ExpenseCategory.CAFE, classify("ｽﾀﾊﾞ"))            // 반각 카타카나
        assertEquals(ExpenseCategory.SUBSCRIPTION, classify("Netflix"))
        assertEquals(ExpenseCategory.TRANSPORT, classify("ＪＲ　定期"))    // 전각 공백
    }

    @Test
    fun `배달 키워드`() {
        assertTrue(classify.isDelivery("ウーバーイーツ"))
        assertTrue(classify.isDelivery("Uber Eats 夕飯"))
        assertTrue(classify.isDelivery("出前館"))
        assertTrue(!classify.isDelivery("ランチ"))
        // 배달 지출은 카테고리론 식비
        assertEquals(ExpenseCategory.FOOD, classify("ウーバーイーツ"))
    }

    @Test
    fun `로케일로 사전을 고른다 - ja 를 켜기 전엔 한국어`() {
        assertEquals(JapaneseExpenseCategoryDictionary, ExpenseCategoryDictionary.forLocale(Locale.JAPAN))
        assertEquals(KoreanExpenseCategoryDictionary, ExpenseCategoryDictionary.forLocale(Locale.KOREA))
        assertEquals(KoreanExpenseCategoryDictionary, ExpenseCategoryDictionary.forLocale(Locale.US))
    }

    @Test
    fun `사전에 중복 키워드가 없다 - 있으면 선언 순서 규칙이 흐려진다`() {
        for (dict in listOf(JapaneseExpenseCategoryDictionary, KoreanExpenseCategoryDictionary)) {
            val all = dict.dictionary.flatMap { (_, words) -> words }
            val dup = all.groupBy { it }.filter { it.value.size > 1 }.keys
            assertTrue("${dict::class.simpleName} 중복: $dup", dup.isEmpty())
            val notNormalized = all.filter { it != ClassifyExpenseCategoryUseCase.normalize(it) }
            assertTrue("${dict::class.simpleName} 정규화 안 된 키워드: $notNormalized", notNormalized.isEmpty())
        }
    }
}
