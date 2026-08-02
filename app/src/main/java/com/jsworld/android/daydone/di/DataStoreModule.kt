package com.jsworld.android.daydone.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jsworld.android.daydone.data.datastore.budgetDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideBudgetDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.budgetDataStore
    }
}