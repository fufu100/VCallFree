package ufree.call.international.phone.wifi.vcallfree.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    val PREF_FILENAME = "translate"
    val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILENAME, 0)

    fun save(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun save(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun save(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun save(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getStringValue(key: String, defaultValue: String): String{
        return prefs.getString(key, defaultValue)!!
    }

    fun getIntValue(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun getLongValue(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }
}