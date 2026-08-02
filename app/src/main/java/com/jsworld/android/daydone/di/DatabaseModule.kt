package com.jsworld.android.daydone.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jsworld.android.daydone.data.local.dao.BackupDao
import com.jsworld.android.daydone.data.local.dao.ExpenseDao
import com.jsworld.android.daydone.data.local.dao.ExtraIncomeDao
import com.jsworld.android.daydone.data.local.dao.FutureExpenseDao
import com.jsworld.android.daydone.data.local.dao.MonthlyBudgetDao
import com.jsworld.android.daydone.data.local.dao.NoSpendChallengeRecordDao
import com.jsworld.android.daydone.data.local.dao.QuickExpenseDao
import com.jsworld.android.daydone.data.local.dao.ScheduledDeductionAmountDao
import com.jsworld.android.daydone.data.local.dao.ScheduledDeductionDao
import com.jsworld.android.daydone.data.local.db.DayDoneDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `monthly_budgets` (
                    `anchorMonth` TEXT NOT NULL,
                    `income` INTEGER NOT NULL,
                    PRIMARY KEY(`anchorMonth`)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `scheduled_deduction_amounts` (
                    `deductionId` INTEGER NOT NULL,
                    `anchorMonth` TEXT NOT NULL,
                    `amount` INTEGER NOT NULL,
                    PRIMARY KEY(`deductionId`, `anchorMonth`)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `future_expenses` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `totalAmount` INTEGER NOT NULL,
                    `targetYearMonth` TEXT NOT NULL,
                    `prepareStartYearMonth` TEXT NOT NULL,
                    `repeatRule` TEXT NOT NULL,
                    `memo` TEXT,
                    `lastPaidYearMonth` TEXT,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `expenses` ADD COLUMN `isEssential` INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `no_spend_records` (
                    `startDate` TEXT NOT NULL,
                    `targetDays` INTEGER NOT NULL,
                    `successDays` INTEGER NOT NULL,
                    `mode` TEXT NOT NULL,
                    `capAmount` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`startDate`)
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideDayDoneDatabase(
        @ApplicationContext context: Context
    ): DayDoneDatabase {
        return Room.databaseBuilder(
            context,
            DayDoneDatabase::class.java,
            "day_done.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideExpenseDao(
        database: DayDoneDatabase
    ): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideScheduledDeductionDao(
        database: DayDoneDatabase
    ): ScheduledDeductionDao {
        return database.scheduledDeductionDao()
    }

    @Provides
    fun provideQuickExpenseDao(
        database: DayDoneDatabase
    ): QuickExpenseDao {
        return database.quickExpenseDao()
    }

    @Provides
    fun provideExtraIncomeDao(
        database: DayDoneDatabase
    ): ExtraIncomeDao {
        return database.extraIncomeDao()
    }

    @Provides
    fun provideMonthlyBudgetDao(
        database: DayDoneDatabase
    ): MonthlyBudgetDao {
        return database.monthlyBudgetDao()
    }

    @Provides
    fun provideScheduledDeductionAmountDao(
        database: DayDoneDatabase
    ): ScheduledDeductionAmountDao {
        return database.scheduledDeductionAmountDao()
    }

    @Provides
    fun provideFutureExpenseDao(
        database: DayDoneDatabase
    ): FutureExpenseDao {
        return database.futureExpenseDao()
    }

    @Provides
    fun provideBackupDao(
        database: DayDoneDatabase
    ): BackupDao {
        return database.backupDao()
    }

    @Provides
    fun provideNoSpendChallengeRecordDao(
        database: DayDoneDatabase
    ): NoSpendChallengeRecordDao {
        return database.noSpendChallengeRecordDao()
    }
}