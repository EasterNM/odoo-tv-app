package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class LocalSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("odoo_settings", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString("base_url", "https://odoo-tv-dashboard.onrender.com") ?: ""
        set(value) = prefs.edit().putString("base_url", value.trim().removeSuffix("/")).apply()

    var useSimulation: Boolean
        get() = prefs.getBoolean("use_simulation", true)
        set(value) = prefs.edit().putBoolean("use_simulation", value).apply()
}
