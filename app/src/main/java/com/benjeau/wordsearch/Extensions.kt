package com.benjeau.wordsearch

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import android.util.TypedValue
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games

/**
 * Stores the specified string at the specified key in the Shared Preferences API
 *
 * @param key The key where the value will be stored
 * @param value The value at the specified key
 */
fun Context.storeSharedPref(key: String, value: String?) {
    val sharedPref: SharedPreferences = getSharedPreferences("crosswordGame", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPref.edit()

    editor.putString(key, value)
    editor.apply()
}

/**
 * Returns the key value stored in the Shared Preferences
 *
 * @param key The key used to the value stored in the Shared Preferences
 */
fun Context.getSharedPrefString(key: String): String? {
    val sharedPref: SharedPreferences = getSharedPreferences("crosswordGame", Context.MODE_PRIVATE)
    return sharedPref.getString(key, null)
}

/**
 * Returns a Palette of colors from the specified bitmap
 *
 * @param bitmap The image that will be extracted colors
 */
fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

/**
 * Converts DPs to pixels
 *
 * @param dp The number of DP to be converted to pixels
 */
fun Context.dpToPx(dp: Int): Int {
    return if (dp < 0) dp else Math.round(dp * this.resources.displayMetrics.density)
}

/**
 * Converts SPs to pixels
 *
 * @param sp The number of SP to be converted to pixels
 */
fun Context.spToPx(sp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        this.resources.displayMetrics
    ).toInt()
}

/**
 * Helper function to show the specified view while animating it
 *
 * @param view The view to animate
 */
fun animateShow(view: View) {
    view.visibility = View.VISIBLE
    view.animate().alpha(1.0f)
}

/**
 * Helper function to hide the specified view while animating it
 *
 * @param view The view to animate
 */
fun animateHide(view: View) {
    view.animate().alpha(0.0f)
    view.visibility = View.GONE
}

/**
 * Signs out the person from Google Play Games
 */
fun Context.googlePlaySignOut() {
    val signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
    signInClient.signOut().addOnCompleteListener(this as Activity) {
        storeSharedPref("profileIconURI", null)
        storeSharedPref("profileName", null)
    }
}

/**
 * The onClick of the profile icon layout which allows the user to sign in or see their achievements
 */
fun Context.profileOnClick() {
    val account = GoogleSignIn.getLastSignedInAccount(this)

    if (account != null && GoogleSignIn.hasPermissions(account)) {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            .achievementsIntent
            .addOnSuccessListener { intent ->
                (this as Activity).startActivityForResult(intent, 9003)
            }
    } else {
        val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        val signInClient = GoogleSignIn.getClient(this, signInOptions)
        (this as Activity).startActivityForResult(signInClient.signInIntent, signInRequestCode)
    }
}

const val signInRequestCode: Int = 9001
