package com.jsworld.android.daydone.data.excel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class XlsxWriterTest {

    private fun sampleWorkbook(): ByteArray = XlsxWriter.build(
        listOf(
            XlsxSheet(
                name = "지출",
                columnWidths = listOf(10, 12, 9),
                rows = listOf(
                    listOf(XlsxCell("2026년 7월 지출 내역", bold = true)),
                    listOf(XlsxCell("예산월", bold = true), XlsxCell("이름", bold = true), XlsxCell("금액", bold = true)),
                    listOf(XlsxCell("2026-07"), XlsxCell("점심"), XlsxCell(-9000L)),
                    emptyList(), // 월 구역 사이 빈 줄
                    listOf(XlsxCell("2026-08"), XlsxCell(null), XlsxCell(12345L))
                )
            ),
            XlsxSheet(
                name = "고정지출",
                rows = listOf(listOf(XlsxCell("특수문자 & <괄호> \"따옴표\"")))
            )
        )
    )

    private fun unzip(bytes: ByteArray): Map<String, String> {
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        return entries
    }

    @Test
    fun `필수 파트가 모두 들어있고 전부 유효한 XML 이다`() {
        val entries = unzip(sampleWorkbook())

        val required = listOf(
            "[Content_Types].xml",
            "_rels/.rels",
            "xl/workbook.xml",
            "xl/_rels/workbook.xml.rels",
            "xl/styles.xml",
            "xl/worksheets/sheet1.xml",
            "xl/worksheets/sheet2.xml"
        )
        required.forEach { assertTrue("누락된 파트: $it", it in entries) }
        assertEquals(required.size, entries.size)

        val factory = DocumentBuilderFactory.newInstance()
        entries.forEach { (name, xml) ->
            runCatching { factory.newDocumentBuilder().parse(xml.byteInputStream()) }
                .onFailure { throw AssertionError("XML 파싱 실패: $name", it) }
        }
    }

    @Test
    fun `숫자 셀·굵은 스타일·XML 이스케이프가 올바르다`() {
        val entries = unzip(sampleWorkbook())
        val sheet1 = entries.getValue("xl/worksheets/sheet1.xml")
        val sheet2 = entries.getValue("xl/worksheets/sheet2.xml")

        // 숫자는 v 태그(숫자 셀), 문자열은 inlineStr
        assertTrue(sheet1.contains("<c r=\"C3\"><v>-9000</v></c>"))
        assertTrue(sheet1.contains("t=\"inlineStr\""))
        // 제목/헤더 행은 굵은 스타일(s="1")
        assertTrue(sheet1.contains("<c r=\"A1\" t=\"inlineStr\" s=\"1\">"))
        // null 셀은 건너뛰고 다음 열 참조가 유지된다 (5행: A5, C5만)
        assertTrue(sheet1.contains("<c r=\"C5\"><v>12345</v></c>"))
        assertTrue(!sheet1.contains("r=\"B5\""))
        // 특수문자 이스케이프
        assertTrue(sheet2.contains("특수문자 &amp; &lt;괄호&gt; &quot;따옴표&quot;"))

        // 시트 이름 등록
        val workbook = entries.getValue("xl/workbook.xml")
        assertTrue(workbook.contains("name=\"지출\""))
        assertTrue(workbook.contains("name=\"고정지출\""))
    }

    @Test
    fun `검증용 샘플 파일을 남긴다`() {
        // 외부 도구(엑셀/파이썬)로 열어볼 수 있게 임시 폴더에 저장
        val file = File(System.getProperty("java.io.tmpdir"), "daydone-xlsxwriter-sample.xlsx")
        file.writeBytes(sampleWorkbook())
        assertTrue(file.length() > 0)
    }
}
