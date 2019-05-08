package com.benjeau.wordsearch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.Games
import com.google.android.gms.games.Player
import com.google.android.gms.tasks.OnCompleteListener

/**
 * Stores the specified string to the specified key in the Shared Preferences API
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
 * Stores the specified integer to the specified key in the Shared Preferences API
 *
 * @param key The key where the value will be stored
 * @param value The value at the specified key
 */
fun Context.storeSharedPref(key: String, value: Int) {
    val sharedPref: SharedPreferences = getSharedPreferences("crosswordGame", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPref.edit()

    editor.putInt(key, value)
    editor.apply()
}

/**
 * Returns the key value stored in the Shared Preferences
 *
 * @param key The key used to the value stored in the Shared Preferences
 */
fun Context.getSharedPrefInt(key: String): Int {
    val sharedPref: SharedPreferences = getSharedPreferences("crosswordGame", Context.MODE_PRIVATE)
    return sharedPref.getInt(key, -1)
}

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
 *
 * @param callback The callback executed after the action of login out is finished
 */
fun Context.googlePlaySignOut(callback: OnCompleteListener<Void>) {
    val signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
    signInClient.signOut().addOnCompleteListener(callback)
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

/**
 * Updates the profile picture and name of the top layout
 *
 * @param firstName The view containing the first name of the person
 * @param lastName The view containing the last name of the person
 * @param profilePicture The view containing the profile picture of the person
 */
fun Context.updateProfileView(profilePicture: ImageView, firstName: TextView, lastName: TextView) {
    // Updates the profile icon
    val uri = getSharedPrefString(profileIconURI)
    if (uri != null) {
        profilePicture.setPadding(0, 0, 0, 0)
        val mgr = ImageManager.create(this)
        mgr.loadImage(profilePicture, Uri.parse(uri))
    } else {
        val profileMargin = dpToPx(10)
        profilePicture.setPadding(profileMargin, profileMargin, profileMargin, profileMargin)
        profilePicture.setImageResource(R.drawable.ic_games_controller_black_40dp)
    }

    // Sets up the profile name
    val profileName = getSharedPrefString(profileName)
    if (profileName != null) {
        val name = profileName.split(" ")
        firstName.text = name[0]
        lastName.text = name[1]
    } else {
        firstName.text = getString(R.string.first_name_placeholder)
        lastName.text = getString(R.string.last_name_placeholder)
    }
}

/**
 * Found in the onActivityResult function to handle the signIn of Google Play Games
 *
 * @param callback The callback executed after the action of login in is finished
 * @param data The data from the signIn intent
 */
fun Context.handleSignInRequest(data: Intent?, callback: OnCompleteListener<Player>) {
    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
    val signInAccount = result.signInAccount

    // Checks if the user has logged in successfully
    if (result.isSuccess && signInAccount != null) {
        val info = Games.getPlayersClient(this, signInAccount)

        info.currentPlayer.addOnCompleteListener(callback)
    }
}

const val signInRequestCode: Int = 9001

/**
 * Constants used as keys for the Shared Preferences
 */
const val profileIconURI = "profileIconURI"
const val profileName = "profileName"
const val numberOfWords = "numberOfWords"
const val bestTimeValue = "bestTimeValue"