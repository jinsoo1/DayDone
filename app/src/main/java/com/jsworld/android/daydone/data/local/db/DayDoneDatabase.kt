package com.jsworld.android.daydone.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jsworld.android.daydone.data.local.dao.BackupDao
import com.jsworld.android.daydone.data.local.dao.ExpenseDao
import com.jsworld.android.daydone.data.local.dao.ExtraIncomeDao
import com.jsworld.android.daydone.data.local.dao.FutureExpenseDao
import com.jsworld.android.daydone.data.local.dao.MonthlyBudgetDao
import com.jsworld.android.daydone.data.local.dao.NoSpendChallengeRecordDao
import com.jsworld.android.daydone.data.local.dao.QuickExpenseDao
import com.jsworld.android.daydone.data.local.dao.ScheduledDeductionAmountDao
import com.jsworld.android.daydone.data.local.dao.ScheduledDeductionDao
import com.jsworld.android.daydone.data.local.entity.ExpenseEntity
import com.jsworld.android.daydone.data.local.entity.ExtraIncomeEntity
import com.jsworld.android.daydone.data.local.entity.FutureExpenseEntity
import com.jsworld.android.daydone.data.local.entity.MonthlyBudgetEntity
import com.jsworld.android.daydone.data.local.entity.NoSpendChallengeRecordEntity
import com.jsworld.android.daydone.data.local.entity.QuickExpenseEntity
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionAmountEntity
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionEntity

@Database(
    entities = [
        ExpenseEntity::class,
        ScheduledDeductionEntity::class,
        QuickExpenseEntity::class,
        ExtraIncomeEntity::class,
        MonthlyBudgetEntity::class,
        ScheduledDeductionAmountEntity::class,
        FutureExpenseEntity::class,
        NoSpendChallengeRecordEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class DayDoneDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun scheduledDeductionDao(): ScheduledDeductionDao

    abstract fun quickExpenseDao(): QuickExpenseDao

    abstract fun extraIncomeDao(): ExtraIncomeDao

    abstract fun monthlyBudgetDao(): MonthlyBudgetDao

    abstract fun scheduledDeductionAmountDao(): ScheduledDeductionAmountDao

    abstract fun futureExpenseDao(): FutureExpenseDao

    abstract fun noSpendChallengeRecordDao(): NoSpendChallengeRecordDao

    abstract fun backupDao(): BackupDao
}