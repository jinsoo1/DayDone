package com.jsworld.android.daydone.di

import com.jsworld.android.daydone.domain.usecase.CalculateTodayDefenseLineUseCase
import com.jsworld.android.daydone.domain.usecase.GetBudgetPeriodForMonthUseCase
import com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.GetTodayDateChipsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetCurrentBudgetPeriodUseCase(): GetCurrentBudgetPeriodUseCase {
        return GetCurrentBudgetPeriodUseCase()
    }

    @Provides
    fun provideGetBudgetPeriodForMonthUseCase(): GetBudgetPeriodForMonthUseCase {
        return GetBudgetPeriodForMonthUseCase()
    }

    @Provides
    fun provideCalculateTodayDefenseLineUseCase(): CalculateTodayDefenseLineUseCase {
        return CalculateTodayDefenseLineUseCase()
    }

    @Provides
    fun provideGetTodayDateChipsUseCase(): GetTodayDateChipsUseCase {
        return GetTodayDateChipsUseCase()
    }
}