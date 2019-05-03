package com.benjeau.wordsearch

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {
    private val sharedPrefName = "crosswordGame"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    fun store(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getValueString(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun clearSharedPreference() {
        editor.clear()
        editor.commit()
    }
}

