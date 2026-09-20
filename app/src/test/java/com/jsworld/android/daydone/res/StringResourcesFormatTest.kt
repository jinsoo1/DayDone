package com.jsworld.android.daydone.res

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * strings.xml 의 모든 문자열이 실제로 포맷 가능한지 — 순수 JVM 으로 검사한다.
 *
 * v1.5 추출 때 `"${percent}%"` 가 `%1$s%` 로 나갔다. 자리표시자가 있는 문자열에서
 * 홀로 남은 `%` 는 `String.format` 이 `UnknownFormatConversionException` 을 던진다 —
 * 리포트 화면이 열리는 순간 크래시. 린트(StringFormatInvalid)는 `%` 뒤가 한글이면
 * 못 잡았다. 그래서 여기서 **모든 로케일의 모든 문자열을 실제로 포맷해본다.**
 *
 * 번역 파일(values-ja 등)도 같이 본다: 키 누락·자리표시자 개수/타입 불일치는
 * 런타임에 빈 문구나 크래시가 된다.
 */
class StringResourcesFormatTest {

    private val resDir = File("src/main/res")
    private val placeholder = Regex("""%(\d+)\$([sd])""")

    private fun parse(file: File): Map<String, String> =
        Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(file.readText())
            .associate { it.groupValues[1] to it.groupValues[2] }

    private fun stringsFiles(): List<File> =
        resDir.listFiles { f -> f.isDirectory && f.name.startsWith("values") }!!
            .map { File(it, "strings.xml") }
            .filter { it.exists() }

    /** AAPT 가 하는 것과 같은 정도의 정리: XML 이스케이프·xliff 태그 제거. */
    private fun clean(raw: String): String = raw
        .replace(Regex("""<xliff:g[^>]*>"""), "")
        .replace("</xliff:g>", "")
        .replace("&#39;", "'").replace("\\'", "'")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")

    private fun dummyArgs(value: String): Array<Any> {
        val slots = placeholder.findAll(value).map { it.groupValues[1].toInt() to it.groupValues[2] }.toList()
        val max = slots.maxOfOrNull { it.first } ?: 0
        return Array(max) { i ->
            val type = slots.firstOrNull { it.first == i + 1 }?.second ?: "s"
            if (type == "d") 7 else "x"
        }
    }

    @Test
    fun `모든 로케일의 모든 문자열이 포맷 가능하다`() {
        val failures = mutableListOf<String>()
        for (file in stringsFiles()) {
            for ((key, raw) in parse(file)) {
                val value = clean(raw)
                if (!placeholder.containsMatchIn(value)) continue
                runCatching { String.format(value, *dummyArgs(value)) }
                    .onFailure { failures += "${file.parentFile.name}/$key: ${it.message} <- $value" }
            }
        }
        assertTrue("포맷할 수 없는 문자열:\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun `번역 파일은 기본 파일과 키와 자리표시자가 같다`() {
        val base = parse(File(resDir, "values/strings.xml"))
        val problems = mutableListOf<String>()
        for (file in stringsFiles()) {
            if (file.parentFile.name == "values") continue
            val other = parse(file)
            (base.keys - other.keys).forEach { problems += "${file.parentFile.name}: 누락 $it" }
            (other.keys - base.keys).forEach { problems += "${file.parentFile.name}: 기본에 없는 키 $it" }
            for (key in base.keys intersect other.keys) {
                val a = placeholder.findAll(clean(base.getValue(key))).map { it.value }.sorted().toList()
                val b = placeholder.findAll(clean(other.getValue(key))).map { it.value }.sorted().toList()
                if (a != b) problems += "${file.parentFile.name}/$key: 자리표시자 $a vs $b"
            }
        }
        assertEquals("번역 파일 불일치:\n" + problems.joinToString("\n"), 0, problems.size)
    }
}
