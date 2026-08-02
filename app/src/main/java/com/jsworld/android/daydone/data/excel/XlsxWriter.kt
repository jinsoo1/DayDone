package com.jsworld.android.daydone.data.excel

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** 셀 하나. 값이 Number 면 숫자 셀, 그 외엔 문자열 셀, null/빈 문자열이면 빈 칸. */
data class XlsxCell(val value: Any?, val bold: Boolean = false)

/** 시트 하나. [columnWidths] 는 엑셀 문자 수 단위 열 너비(비우면 기본 폭). */
data class XlsxSheet(
    val name: String,
    val columnWidths: List<Int> = emptyList(),
    val rows: List<List<XlsxCell>>
)

/**
 * xlsx 파일을 라이브러리 없이 만든다 — xlsx 는 XML 몇 개를 담은 ZIP 이라
 * POI 같은 무거운 의존성 없이 이 정도로 충분하다(엑셀·구글시트·한셀에서 열림).
 * 문자열은 inlineStr 로 넣어 sharedStrings 파트를 생략한다.
 */
object XlsxWriter {

    fun build(sheets: List<XlsxSheet>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.put("[Content_Types].xml", contentTypesXml(sheets.size))
            zip.put("_rels/.rels", ROOT_RELS)
            zip.put("xl/workbook.xml", workbookXml(sheets))
            zip.put("xl/_rels/workbook.xml.rels", workbookRelsXml(sheets.size))
            zip.put("xl/styles.xml", STYLES_XML)
            sheets.forEachIndexed { index, sheet ->
                zip.put("xl/worksheets/sheet${index + 1}.xml", sheetXml(sheet))
            }
        }
        return out.toByteArray()
    }

    private fun ZipOutputStream.put(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun contentTypesXml(sheetCount: Int): String = buildString {
        append(XML_HEADER)
        append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
        append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
        append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
        append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
        append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
        repeat(sheetCount) { i ->
            append("<Override PartName=\"/xl/worksheets/sheet${i + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
        }
        append("</Types>")
    }

    private fun workbookXml(sheets: List<XlsxSheet>): String = buildString {
        append(XML_HEADER)
        append("<workbook xmlns=\"$NS_MAIN\" xmlns:r=\"$NS_RELS\"><sheets>")
        sheets.forEachIndexed { index, sheet ->
            append("<sheet name=\"${escapeXml(sheet.name)}\" sheetId=\"${index + 1}\" r:id=\"rId${index + 1}\"/>")
        }
        append("</sheets></workbook>")
    }

    private fun workbookRelsXml(sheetCount: Int): String = buildString {
        append(XML_HEADER)
        append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
        repeat(sheetCount) { i ->
            append("<Relationship Id=\"rId${i + 1}\" Type=\"$NS_RELS/worksheet\" Target=\"worksheets/sheet${i + 1}.xml\"/>")
        }
        append("<Relationship Id=\"rId${sheetCount + 1}\" Type=\"$NS_RELS/styles\" Target=\"styles.xml\"/>")
        append("</Relationships>")
    }

    private fun sheetXml(sheet: XlsxSheet): String = buildString {
        append(XML_HEADER)
        append("<worksheet xmlns=\"$NS_MAIN\">")
        if (sheet.columnWidths.isNotEmpty()) {
            append("<cols>")
            sheet.columnWidths.forEachIndexed { index, width ->
                append("<col min=\"${index + 1}\" max=\"${index + 1}\" width=\"$width\" customWidth=\"1\"/>")
            }
            append("</cols>")
        }
        append("<sheetData>")
        sheet.rows.forEachIndexed { rowIndex, row ->
            append("<row r=\"${rowIndex + 1}\">")
            row.forEachIndexed { colIndex, cell ->
                appendCell(cell, rowIndex, colIndex)
            }
            append("</row>")
        }
        append("</sheetData></worksheet>")
    }

    private fun StringBuilder.appendCell(cell: XlsxCell, rowIndex: Int, colIndex: Int) {
        val value = cell.value ?: return
        val ref = "${columnName(colIndex)}${rowIndex + 1}"
        val style = if (cell.bold) " s=\"1\"" else ""
        if (value is Number) {
            append("<c r=\"$ref\"$style><v>$value</v></c>")
            return
        }
        val text = value.toString()
        if (text.isEmpty()) return
        append("<c r=\"$ref\" t=\"inlineStr\"$style><is><t xml:space=\"preserve\">${escapeXml(text)}</t></is></c>")
    }

    /** 0 → A, 25 → Z, 26 → AA */
    private fun columnName(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i >= 0) {
            sb.insert(0, ('A' + i % 26))
            i = i / 26 - 1
        }
        return sb.toString()
    }

    private fun escapeXml(text: String): String = buildString(text.length) {
        text.forEach { ch ->
            when (ch) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(ch)
            }
        }
    }

    private const val XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    private const val NS_MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    private const val NS_RELS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

    private const val ROOT_RELS = XML_HEADER +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"$NS_RELS/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "</Relationships>"

    // fontId 1 = 굵게(s="1"). 두 번째 fill(gray125)은 엑셀 규격이 요구하는 자리 채움.
    private const val STYLES_XML = XML_HEADER +
            "<styleSheet xmlns=\"$NS_MAIN\">" +
            "<fonts count=\"2\">" +
            "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "</fonts>" +
            "<fills count=\"2\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "</fills>" +
            "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"2\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
            "</styleSheet>"
}
