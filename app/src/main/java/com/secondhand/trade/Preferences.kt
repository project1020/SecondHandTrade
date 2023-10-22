package com.secondhand.trade

import android.content.Context

object Preferences {
    private const val PREF_NAME = "MyPrefs"
    private val sharedPreferences = MyApplication.instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isAutoLogin: Boolean
        get() = sharedPreferences.getBoolean("isAutoLogin", false)
        set(value) = sharedPreferences.edit().putBoolean("isAutoLogin", value).apply()
}