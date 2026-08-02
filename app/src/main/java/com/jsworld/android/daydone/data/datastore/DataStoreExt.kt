package com.jsworld.android.daydone.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.budgetDataStore by preferencesDataStore(
    name = "budget_profile"
)