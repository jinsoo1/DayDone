package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.ExpenseEntity
import com.jsworld.android.daydone.data.local.entity.ExtraIncomeEntity
import com.jsworld.android.daydone.data.local.entity.FutureExpenseEntity
import com.jsworld.android.daydone.data.local.entity.HeldPurchaseEntity
import com.jsworld.android.daydone.data.local.entity.MonthlyBudgetEntity
import com.jsworld.android.daydone.data.local.entity.NoSpendChallengeRecordEntity
import com.jsworld.android.daydone.data.local.entity.QuickExpenseEntity
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionAmountEntity
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionEntity

/**
 * 데이터 내보내기/가져오기 전용 DAO.
 * 전 테이블을 한 번에 읽고 쓰기 위해 별도로 둔다(기존 DAO 는 화면용 그대로 유지).
 * 가져오기는 id 를 그대로 넣어 항목 간 연결(futureExpenseId, deductionId)을 보존한다.
 */
@Dao
interface BackupDao {

    @Query("SELECT * FROM expenses")
    suspend fun getExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM scheduled_deductions")
    suspend fun getScheduledDeductions(): List<ScheduledDeductionEntity>

    @Query("SELECT * FROM scheduled_deduction_amounts")
    suspend fun getDeductionAmounts(): List<ScheduledDeductionAmountEntity>

    @Query("SELECT * FROM extra_incomes")
    suspend fun getExtraIncomes(): List<ExtraIncomeEntity>

    @Query("SELECT * FROM monthly_budgets")
    suspend fun getMonthlyBudgets(): List<MonthlyBudgetEntity>

    @Query("SELECT * FROM quick_expenses")
    suspend fun getQuickExpenses(): List<QuickExpenseEntity>

    @Query("SELECT * FROM future_expenses")
    suspend fun getFutureExpenses(): List<FutureExpenseEntity>

    @Query("SELECT * FROM no_spend_records")
    suspend fun getNoSpendRecords(): List<NoSpendChallengeRecordEntity>

    @Query("SELECT * FROM held_purchases")
    suspend fun getHeldPurchases(): List<HeldPurchaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(items: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledDeductions(items: List<ScheduledDeductionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeductionAmounts(items: List<ScheduledDeductionAmountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtraIncomes(items: List<ExtraIncomeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyBudgets(items: List<MonthlyBudgetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickExpenses(items: List<QuickExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFutureExpenses(items: List<FutureExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoSpendRecords(items: List<NoSpendChallengeRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeldPurchases(items: List<HeldPurchaseEntity>)
}
