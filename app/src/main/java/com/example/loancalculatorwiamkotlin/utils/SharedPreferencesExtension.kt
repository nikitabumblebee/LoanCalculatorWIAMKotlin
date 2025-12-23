package com.example.loancalculatorwiamkotlin.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "loan_prefs"
    private const val KEY_LAST_AMOUNT = "lastAmount"
    private const val KEY_LAST_PERIOD = "lastPeriod"

    fun saveLastAmount(context: Context, amount: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_LAST_AMOUNT, amount.toFloat()).apply()
    }

    fun getLastAmount(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_LAST_AMOUNT, 10_000f).toDouble()
    }

    fun saveLastPeriod(context: Context, period: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_LAST_PERIOD, period).apply()
    }

    fun getLastPeriod(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LAST_PERIOD, 14)
    }
}
