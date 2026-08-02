package com.jsworld.android.daydone.di

import com.jsworld.android.daydone.data.repository.BudgetProfileRepositoryImpl
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import com.jsworld.android.daydone.data.repository.ExpenseRepositoryImpl
import com.jsworld.android.daydone.data.repository.BackupRepositoryImpl
import com.jsworld.android.daydone.data.repository.DataResetRepositoryImpl
import com.jsworld.android.daydone.data.repository.ExtraIncomeRepositoryImpl
import com.jsworld.android.daydone.data.repository.FutureExpenseRepositoryImpl
import com.jsworld.android.daydone.data.repository.MonthlyBudgetRepositoryImpl
import com.jsworld.android.daydone.data.repository.NoSpendChallengeRecordRepositoryImpl
import com.jsworld.android.daydone.data.repository.NoSpendChallengeRepositoryImpl
import com.jsworld.android.daydone.data.repository.NoticeRepositoryImpl
import com.jsworld.android.daydone.data.repository.QuickExpenseRepositoryImpl
import com.jsworld.android.daydone.data.repository.ScheduledDeductionAmountRepositoryImpl
import com.jsworld.android.daydone.data.repository.ScheduledDeductionRepositoryImpl
import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import com.jsworld.android.daydone.domain.repository.BackupRepository
import com.jsworld.android.daydone.domain.repository.DataResetRepository
import com.jsworld.android.daydone.domain.repository.ExtraIncomeRepository
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import com.jsworld.android.daydone.domain.repository.MonthlyBudgetRepository
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRecordRepository
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRepository
import com.jsworld.android.daydone.domain.repository.NoticeRepository
import com.jsworld.android.daydone.domain.repository.QuickExpenseRepository
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionAmountRepository
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetProfileRepository(
        impl: BudgetProfileRepositoryImpl
    ): BudgetProfileRepository

    @Binds
    @Singleton
    abstract fun bindScheduledDeductionRepository(
        impl: ScheduledDeductionRepositoryImpl
    ): ScheduledDeductionRepository

    @Binds
    @Singleton
    abstract fun bindQuickExpenseRepository(
        impl: QuickExpenseRepositoryImpl
    ): QuickExpenseRepository

    @Binds
    @Singleton
    abstract fun bindExtraIncomeRepository(
        impl: ExtraIncomeRepositoryImpl
    ): ExtraIncomeRepository

    @Binds
    @Singleton
    abstract fun bindMonthlyBudgetRepository(
        impl: MonthlyBudgetRepositoryImpl
    ): MonthlyBudgetRepository

    @Binds
    @Singleton
    abstract fun bindScheduledDeductionAmountRepository(
        impl: ScheduledDeductionAmountRepositoryImpl
    ): ScheduledDeductionAmountRepository

    @Binds
    @Singleton
    abstract fun bindFutureExpenseRepository(
        impl: FutureExpenseRepositoryImpl
    ): FutureExpenseRepository

    @Binds
    @Singleton
    abstract fun bindDataResetRepository(
        impl: DataResetRepositoryImpl
    ): DataResetRepository

    @Binds
    @Singleton
    abstract fun bindNoSpendChallengeRepository(
        impl: NoSpendChallengeRepositoryImpl
    ): NoSpendChallengeRepository

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(
        impl: NoticeRepositoryImpl
    ): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: BackupRepositoryImpl
    ): BackupRepository

    @Binds
    @Singleton
    abstract fun bindNoSpendChallengeRecordRepository(
        impl: NoSpendChallengeRecordRepositoryImpl
    ): NoSpendChallengeRecordRepository
}