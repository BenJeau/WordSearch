package com.benjeau.wordsearch

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import android.util.TypedValue

fun Context.storeSharedPref(key: String, value: String?) {
    val sharedPref: SharedPreferences = getSharedPreferences("crosswordGame", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPref.edit()

    editor.putString(key, value)
    editor.apply()
}

fun Context.getSharedPrefString(key: String): String? {
    val sharedPref: SharedPreferences = getSharedPreferences("crosswordGame", Context.MODE_PRIVATE)
    return sharedPref.getString(key, null)
}

fun Context.dpToPx(dp: Int): Int {
    return if (dp < 0) dp else Math.round(dp * this.resources.displayMetrics.density)
}

fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

fun Context.spToPx(sp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        this.resources.displayMetrics
    ).toInt()
}