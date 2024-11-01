package com.kalltech.currenc_converter.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    fun applyTheme(themePref: Int) {
        when (themePref) {
            ThemeConstants.THEME_MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeConstants.THEME_MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun getSavedThemePref(context: Context): Int {
        val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return sharedPref.getInt(ThemeConstants.THEME_PREFERENCE, ThemeConstants.THEME_MODE_SYSTEM)
    }

    fun saveThemePref(context: Context, themePref: Int) {
        val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(ThemeConstants.THEME_PREFERENCE, themePref)
            apply()
        }
    }
}
