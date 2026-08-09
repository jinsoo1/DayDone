package com.jsworld.android.daydone.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.jsworld.android.daydone.data.datastore.BudgetProfileDataSource
import com.jsworld.android.daydone.data.excel.XlsxCell
import com.jsworld.android.daydone.data.excel.XlsxSheet
import com.jsworld.android.daydone.data.excel.XlsxWriter
import com.jsworld.android.daydone.data.datastore.NoSpendChallengeDataSource
import com.jsworld.android.daydone.data.local.dao.BackupDao
import com.jsworld.android.daydone.data.local.db.DayDoneDatabase
import com.jsworld.android.daydone.data.local.entity.ExpenseEntity
import com.jsworld.android.daydone.data.local.entity.ExtraIncomeEntity
import com.jsworld.android.daydone.data.local.entity.FutureExpenseEntity
import com.jsworld.android.daydone.data.local.entity.MonthlyBudgetEntity
import com.jsworld.android.daydone.data.local.entity.NoSpendChallengeRecordEntity
import com.jsworld.android.daydone.data.local.entity.QuickExpenseEntity
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionAmountEntity
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionEntity
import com.jsworld.android.daydone.domain.model.BackupFileInfo
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.repository.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * 전체 데이터를 JSON 한 파일로 주고받는다.
 * - id 를 그대로 보존해 항목 간 연결(futureExpenseId, deductionId)이 유지된다.
 * - 가져오기는 "전체 대체" (clearAllTables 후 삽입).
 */
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: DayDoneDatabase,
    private val backupDao: BackupDao,
    private val budgetProfileDataSource: BudgetProfileDataSource,
    private val noSpendChallengeDataSource: NoSpendChallengeDataSource
) : BackupRepository {

    /** 다운로드/DayDone 폴더에 바로 저장 (Android 10+ 는 권한 불필요). */
    override suspend fun exportToDownloads(): String = withContext(Dispatchers.IO) {
        val json = exportToJson()
        saveToDownloads(
            subFolder = FOLDER_BACKUP,
            fileName = datedFileName("daydone-backup", "json"),
            mimeType = "application/json",
            content = json.toByteArray()
        )
    }

    /**
     * 지출 내역(월별 구역)과 고정 지출, 시트 2개짜리 엑셀(xlsx) 파일을 저장하고 경로를 돌려준다.
     */
    override suspend fun exportExcelToDownloads(): String = withContext(Dispatchers.IO) {
        val workbook = XlsxWriter.build(listOf(buildHistorySheet(), buildDeductionSheet()))
        saveToDownloads(
            subFolder = FOLDER_EXCEL,
            fileName = datedFileName("daydone-내역", "xlsx"),
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            content = workbook
        )
    }

    /** 지출 시트 — 예산월별로 "2026년 7월 지출 내역" 제목 + 표 구역을 이어 붙인다. */
    private suspend fun buildHistorySheet(): XlsxSheet {
        val budgetStartDay = budgetProfileDataSource.budgetProfileFlow.first().budgetStartDay

        val rows = buildList {
            backupDao.getExpenses().forEach { e ->
                val kind = if (e.type == "FUTURE_PREPARE") "준비금" else "지출"
                add(HistoryRow(e.date, kind, e.title, -e.amount, e.isEssential, e.memo))
            }
            backupDao.getExtraIncomes().forEach { i ->
                add(HistoryRow(i.date, "추가수익", i.title, i.amount, false, i.memo))
            }
        }.sortedBy { it.date }

        val header = listOf("예산월", "날짜", "구분", "이름", "금액", "필수 지출", "메모")
        val sheetRows = buildList {
            if (rows.isEmpty()) {
                add(header.map { XlsxCell(it, bold = true) })
            }
            rows.groupBy { anchorMonthOf(it.date, budgetStartDay) }.forEach { (month, monthRows) ->
                add(listOf(XlsxCell(monthTitle(month), bold = true)))
                add(header.map { XlsxCell(it, bold = true) })
                monthRows.forEach { row ->
                    add(
                        listOf(
                            XlsxCell(month),
                            XlsxCell(row.date),
                            XlsxCell(row.kind),
                            XlsxCell(row.title),
                            XlsxCell(row.amount),
                            XlsxCell(if (row.isEssential) "예" else null),
                            XlsxCell(row.memo)
                        )
                    )
                }
                add(emptyList()) // 월 구역 사이 빈 줄
            }
        }

        return XlsxSheet(
            name = "지출",
            columnWidths = listOf(10, 12, 9, 18, 12, 9, 24),
            rows = sheetRows
        )
    }

    private suspend fun buildDeductionSheet(): XlsxSheet {
        // 금액은 화면과 같은 규칙: 가장 최근 월별 오버라이드, 없으면 최초 금액
        val latestOverrides = backupDao.getDeductionAmounts()
            .groupBy { it.deductionId }
            .mapValues { (_, list) -> list.maxBy { it.anchorMonth }.amount }

        val deductions = backupDao.getScheduledDeductions()
            .sortedWith(compareBy({ it.type != "SAVING" }, { it.withdrawalDay }))

        val header = listOf("구분", "이름", "금액", "출금일", "시작월", "종료월", "메모")
        val sheetRows = buildList {
            add(header.map { XlsxCell(it, bold = true) })
            deductions.forEach { d ->
                add(
                    listOf(
                        XlsxCell(if (d.type == "SAVING") "저축" else "고정비"),
                        XlsxCell(d.title),
                        XlsxCell(latestOverrides[d.id] ?: d.amount),
                        XlsxCell(d.withdrawalDay),
                        XlsxCell(d.startYearMonth),
                        XlsxCell(d.endYearMonth),
                        XlsxCell(d.memo)
                    )
                )
            }
        }

        return XlsxSheet(
            name = "고정지출",
            columnWidths = listOf(8, 18, 12, 8, 10, 10, 24),
            rows = sheetRows
        )
    }

    /** "2026-07" → "2026년 7월 지출 내역" */
    private fun monthTitle(anchorMonth: String): String {
        val year = anchorMonth.take(4)
        val month = anchorMonth.drop(5).toIntOrNull()?.toString() ?: anchorMonth.drop(5)
        return "${year}년 ${month}월 지출 내역"
    }

    /**
     * 다운로드/DayDone 폴더의 .json 백업 목록 (최신순).
     * MediaStore 는 이 앱이 만든 파일만 돌려주므로, 재설치·기기 변경 후에는
     * 파일이 폴더에 있어도 빈 목록일 수 있다 — 그때는 파일 선택창으로 안내한다.
     */
    override suspend fun listBackupFiles(): List<BackupFileInfo> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext emptyList()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE
        )
        // 하위 폴더 도입 전(DayDone/ 바로 아래)에 만든 백업도 함께 잡는다
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? " +
                "AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(
            "${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_FOLDER/%",
            "%.json"
        )

        runCatching {
            context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        add(
                            BackupFileInfo(
                                name = cursor.getString(1).orEmpty(),
                                uri = ContentUris.withAppendedId(
                                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, id
                                ).toString(),
                                modifiedAtMillis = cursor.getLong(2) * 1000L,
                                sizeBytes = cursor.getLong(3)
                            )
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    override suspend fun importFromFile(uri: String): Unit = withContext(Dispatchers.IO) {
        val json = context.contentResolver.openInputStream(Uri.parse(uri))
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("파일을 읽을 수 없어요. 폴더에서 직접 선택해 주세요.")
        importFromJson(json)
    }

    /**
     * 지출 날짜가 속한 예산 기간의 anchorMonth("YYYY-MM").
     * GetCurrentBudgetPeriodUseCase 와 같은 규칙: 일(day)이 시작일 이상이면 그 달, 아니면 전 달.
     * 앱의 월 탭에서 보는 "몇 월" 과 일치한다.
     */
    private fun anchorMonthOf(date: String, budgetStartDay: Int): String =
        runCatching {
            val d = LocalDate.parse(date)
            val ym = java.time.YearMonth.from(d)
            // 시작일이 그 달에 없으면 말일로 clamp (기간 UseCase와 같은 규칙 — 말일 시작 지원)
            val startInMonth = minOf(budgetStartDay, ym.lengthOfMonth())
            val anchor = if (d.dayOfMonth >= startInMonth) ym else ym.minusMonths(1)
            "%04d-%02d".format(anchor.year, anchor.monthValue)
        }.getOrDefault(date.take(7))

    private data class HistoryRow(
        val date: String,
        val kind: String,
        val title: String,
        val amount: Long,
        val isEssential: Boolean,
        val memo: String?
    )


    private fun datedFileName(prefix: String, extension: String): String {
        val today = LocalDate.now()
        return "%s-%04d%02d%02d.%s".format(
            prefix, today.year, today.monthValue, today.dayOfMonth, extension
        )
    }

    private fun saveToDownloads(
        subFolder: String,
        fileName: String,
        mimeType: String,
        content: ByteArray
    ): String {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "이 기기에서는 폴더를 직접 만들 수 없어요."
        }

        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_FOLDER/$subFolder"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("저장할 위치를 만들 수 없어요.")

        resolver.openOutputStream(uri)?.use { out ->
            out.write(content)
        } ?: error("파일을 쓸 수 없어요.")

        // 같은 이름이 있으면 시스템이 (1) 을 붙이므로 실제 이름을 다시 읽는다
        val savedName = resolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: fileName

        return "$relativePath/$savedName"
    }

    override suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val profile = budgetProfileDataSource.budgetProfileFlow.first()
        val onboardingDone = budgetProfileDataSource.isOnboardingDoneFlow.first()
        val preJoinHandled = budgetProfileDataSource.isPreJoinSpendHandledFlow.first()
        val challenge = noSpendChallengeDataSource.settingsFlow.first()

        val root = JSONObject().apply {
            put(KEY_VERSION, BACKUP_VERSION)
            put(KEY_APP, "daydone")
            put(KEY_EXPORTED_AT, System.currentTimeMillis())

            put(
                KEY_SETTINGS,
                JSONObject().apply {
                    put("monthlyIncome", profile.monthlyIncome)
                    put("payday", profile.payday)
                    put("budgetStartDay", profile.budgetStartDay)
                    put("firstUseDate", profile.firstUseDate?.toString() ?: JSONObject.NULL)
                    put("onboardingDone", onboardingDone)
                    put("preJoinSpendHandled", preJoinHandled)
                    put("challengeEnabled", challenge.enabled)
                    put("challengeMode", challenge.mode.name)
                    put("challengeCapAmount", challenge.capAmount)
                    put("challengeTargetDays", challenge.targetDays)
                    put("challengeStartDate", challenge.startDate?.toString() ?: JSONObject.NULL)
                }
            )

            put(KEY_EXPENSES, backupDao.getExpenses().toJsonArray { e ->
                JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("amount", e.amount)
                    put("date", e.date)
                    put("type", e.type)
                    put("futureExpenseId", e.futureExpenseId ?: JSONObject.NULL)
                    put("memo", e.memo ?: JSONObject.NULL)
                    put("isEssential", e.isEssential)
                    put("createdAt", e.createdAt)
                }
            })

            put(KEY_DEDUCTIONS, backupDao.getScheduledDeductions().toJsonArray { d ->
                JSONObject().apply {
                    put("id", d.id)
                    put("title", d.title)
                    put("amount", d.amount)
                    put("type", d.type)
                    put("withdrawalDay", d.withdrawalDay)
                    put("startYearMonth", d.startYearMonth)
                    put("endYearMonth", d.endYearMonth ?: JSONObject.NULL)
                    put("memo", d.memo ?: JSONObject.NULL)
                    put("createdAt", d.createdAt)
                }
            })

            put(KEY_DEDUCTION_AMOUNTS, backupDao.getDeductionAmounts().toJsonArray { a ->
                JSONObject().apply {
                    put("deductionId", a.deductionId)
                    put("anchorMonth", a.anchorMonth)
                    put("amount", a.amount)
                }
            })

            put(KEY_EXTRA_INCOMES, backupDao.getExtraIncomes().toJsonArray { i ->
                JSONObject().apply {
                    put("id", i.id)
                    put("title", i.title)
                    put("amount", i.amount)
                    put("date", i.date)
                    put("memo", i.memo ?: JSONObject.NULL)
                    put("createdAt", i.createdAt)
                }
            })

            put(KEY_MONTHLY_BUDGETS, backupDao.getMonthlyBudgets().toJsonArray { b ->
                JSONObject().apply {
                    put("anchorMonth", b.anchorMonth)
                    put("income", b.income)
                }
            })

            put(KEY_QUICK_EXPENSES, backupDao.getQuickExpenses().toJsonArray { q ->
                JSONObject().apply {
                    put("id", q.id)
                    put("title", q.title)
                    put("amount", q.amount)
                    put("sortOrder", q.sortOrder)
                    put("isActive", q.isActive)
                    put("createdAt", q.createdAt)
                }
            })

            put(KEY_FUTURE_EXPENSES, backupDao.getFutureExpenses().toJsonArray { f ->
                JSONObject().apply {
                    put("id", f.id)
                    put("title", f.title)
                    put("category", f.category)
                    put("totalAmount", f.totalAmount)
                    put("targetYearMonth", f.targetYearMonth)
                    put("prepareStartYearMonth", f.prepareStartYearMonth)
                    put("repeatRule", f.repeatRule)
                    put("memo", f.memo ?: JSONObject.NULL)
                    put("lastPaidYearMonth", f.lastPaidYearMonth ?: JSONObject.NULL)
                    put("createdAt", f.createdAt)
                }
            })

            put(KEY_NO_SPEND_RECORDS, backupDao.getNoSpendRecords().toJsonArray { r ->
                JSONObject().apply {
                    put("startDate", r.startDate)
                    put("targetDays", r.targetDays)
                    put("successDays", r.successDays)
                    put("mode", r.mode)
                    put("capAmount", r.capAmount)
                    put("createdAt", r.createdAt)
                }
            })
        }

        root.toString(2)
    }

    override suspend fun importFromJson(json: String): Unit = withContext(Dispatchers.IO) {
        val root = JSONObject(json)

        require(root.optString(KEY_APP) == "daydone") {
            "데이던 백업 파일이 아니에요."
        }
        require(root.optInt(KEY_VERSION, 0) in 1..BACKUP_VERSION) {
            "지원하지 않는 백업 버전이에요."
        }

        // 형식을 먼저 다 읽어 검증한 뒤 지운다 (중간 실패로 데이터가 비는 것 방지)
        val expenses = root.array(KEY_EXPENSES).map { o ->
            ExpenseEntity(
                id = o.getLong("id"),
                title = o.getString("title"),
                amount = o.getLong("amount"),
                date = o.getString("date"),
                type = o.getString("type"),
                futureExpenseId = o.longOrNull("futureExpenseId"),
                memo = o.stringOrNull("memo"),
                isEssential = o.optBoolean("isEssential", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val deductions = root.array(KEY_DEDUCTIONS).map { o ->
            ScheduledDeductionEntity(
                id = o.getLong("id"),
                title = o.getString("title"),
                amount = o.getLong("amount"),
                type = o.getString("type"),
                withdrawalDay = o.getInt("withdrawalDay"),
                startYearMonth = o.getString("startYearMonth"),
                endYearMonth = o.stringOrNull("endYearMonth"),
                memo = o.stringOrNull("memo"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val deductionAmounts = root.array(KEY_DEDUCTION_AMOUNTS).map { o ->
            ScheduledDeductionAmountEntity(
                deductionId = o.getLong("deductionId"),
                anchorMonth = o.getString("anchorMonth"),
                amount = o.getLong("amount")
            )
        }
        val extraIncomes = root.array(KEY_EXTRA_INCOMES).map { o ->
            ExtraIncomeEntity(
                id = o.getLong("id"),
                title = o.getString("title"),
                amount = o.getLong("amount"),
                date = o.getString("date"),
                memo = o.stringOrNull("memo"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val monthlyBudgets = root.array(KEY_MONTHLY_BUDGETS).map { o ->
            MonthlyBudgetEntity(
                anchorMonth = o.getString("anchorMonth"),
                income = o.getLong("income")
            )
        }
        val quickExpenses = root.array(KEY_QUICK_EXPENSES).map { o ->
            QuickExpenseEntity(
                id = o.getLong("id"),
                title = o.getString("title"),
                amount = o.getLong("amount"),
                sortOrder = o.optInt("sortOrder", 0),
                isActive = o.optBoolean("isActive", true),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val futureExpenses = root.array(KEY_FUTURE_EXPENSES).map { o ->
            FutureExpenseEntity(
                id = o.getLong("id"),
                title = o.getString("title"),
                category = o.getString("category"),
                totalAmount = o.getLong("totalAmount"),
                targetYearMonth = o.getString("targetYearMonth"),
                prepareStartYearMonth = o.getString("prepareStartYearMonth"),
                repeatRule = o.getString("repeatRule"),
                memo = o.stringOrNull("memo"),
                lastPaidYearMonth = o.stringOrNull("lastPaidYearMonth"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val noSpendRecords = root.array(KEY_NO_SPEND_RECORDS).map { o ->
            NoSpendChallengeRecordEntity(
                startDate = o.getString("startDate"),
                targetDays = o.getInt("targetDays"),
                successDays = o.getInt("successDays"),
                mode = o.getString("mode"),
                capAmount = o.optLong("capAmount", 0L),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }

        // 전체 대체
        database.clearAllTables()
        backupDao.insertScheduledDeductions(deductions)
        backupDao.insertDeductionAmounts(deductionAmounts)
        backupDao.insertFutureExpenses(futureExpenses)
        backupDao.insertExpenses(expenses)
        backupDao.insertExtraIncomes(extraIncomes)
        backupDao.insertMonthlyBudgets(monthlyBudgets)
        backupDao.insertQuickExpenses(quickExpenses)
        backupDao.insertNoSpendRecords(noSpendRecords)

        // 설정 복원
        root.optJSONObject(KEY_SETTINGS)?.let { s ->
            budgetProfileDataSource.updateMonthlyIncome(s.optLong("monthlyIncome", 3_000_000L))
            budgetProfileDataSource.updatePayday(s.optInt("payday", 25))
            budgetProfileDataSource.updateBudgetStartDay(s.optInt("budgetStartDay", 1))

            // 첫 사용일은 백업 그대로 되살린다 (복원 = 재설치 전 상태 복원).
            // 값이 없으면(온보딩 기능 이전 레거시 설치) 비운다 — 재설치 온보딩 날짜를
            // 남겨두면 무지출 일수가 그날부터 세어져 어긋난다. null = 제한 없음이 원래 상태.
            val backupFirstUse = s.stringOrNull("firstUseDate")
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (backupFirstUse != null) {
                budgetProfileDataSource.updateFirstUseDate(backupFirstUse)
            } else {
                budgetProfileDataSource.clearFirstUseDate()
            }

            if (s.optBoolean("onboardingDone", true)) {
                budgetProfileDataSource.setOnboardingDone()
            }
            // 복원한 유저는 기록을 통째로 가져온 상태라 "가입 전 지출" 배너 대상이 아니다
            budgetProfileDataSource.setPreJoinSpendHandled()

            noSpendChallengeDataSource.update(
                NoSpendChallengeSettings(
                    enabled = s.optBoolean("challengeEnabled", false),
                    mode = runCatching { NoSpendMode.valueOf(s.optString("challengeMode")) }
                        .getOrDefault(NoSpendMode.ESSENTIAL_ALLOWED),
                    capAmount = s.optLong("challengeCapAmount", 10_000L),
                    targetDays = s.optInt("challengeTargetDays", 10),
                    startDate = s.stringOrNull("challengeStartDate")
                        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                )
            )
        }
    }

    private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray =
        JSONArray().also { array -> forEach { array.put(transform(it)) } }

    private fun JSONObject.array(key: String): List<JSONObject> {
        val array = optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).map { array.getJSONObject(it) }
    }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).ifBlank { null }

    private fun JSONObject.longOrNull(key: String): Long? =
        if (isNull(key)) null else optLong(key)

    companion object {
        private const val BACKUP_VERSION = 1
        private const val BACKUP_FOLDER = "DayDone"
        private const val FOLDER_BACKUP = "백업"
        private const val FOLDER_EXCEL = "엑셀"

        private const val KEY_VERSION = "backupVersion"
        private const val KEY_APP = "app"
        private const val KEY_EXPORTED_AT = "exportedAt"
        private const val KEY_SETTINGS = "settings"
        private const val KEY_EXPENSES = "expenses"
        private const val KEY_DEDUCTIONS = "scheduledDeductions"
        private const val KEY_DEDUCTION_AMOUNTS = "scheduledDeductionAmounts"
        private const val KEY_EXTRA_INCOMES = "extraIncomes"
        private const val KEY_MONTHLY_BUDGETS = "monthlyBudgets"
        private const val KEY_QUICK_EXPENSES = "quickExpenses"
        private const val KEY_FUTURE_EXPENSES = "futureExpenses"
        private const val KEY_NO_SPEND_RECORDS = "noSpendRecords"
    }
}
