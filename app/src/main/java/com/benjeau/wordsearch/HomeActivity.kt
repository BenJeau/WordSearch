package com.benjeau.wordsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.TextView
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.Games

class HomeActivity : AppCompatActivity() {

    /**
     * References to the views in the layout
     */
    private lateinit var bestTimeText: TextView
    private lateinit var profileIcon: ImageView
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var signOutButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setUpViews()
        updateProfileInfo()

        // Goes to the next screen (game)
        val playGame: Button = findViewById(R.id.playGame)
        playGame.setOnClickListener {
            val myIntent = Intent(this, GameActivity::class.java)
            startActivity(myIntent)
        }

        // Shows prompt to sign in Google Play Games
        val profileLayout: ConstraintLayout = findViewById(R.id.profilePictureLayout)
        profileLayout.setOnClickListener {
            profileOnClick()
        }

        signOutButton.setOnClickListener { googlePlaySignOut() }
    }

    /**
     * Create references to the views often used
     */
    private fun setUpViews() {
        bestTimeText = findViewById(R.id.bestTime)
        profileIcon = findViewById(R.id.profileIcon)
        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)
        signOutButton = findViewById(R.id.signOut)
    }

    /**
     * Gets the value of the best time from shared preferences and displays it
     */
    private fun updateBestTime() {
        bestTimeText.text = if (getSharedPrefString("bestTime") == null) {
            "N.A."
        } else {
            getSharedPrefString("bestTime") + " s."
        }
    }

    /**
     * Puts the information about the signed in user, if the user is signed in Google Play Games
     */
    private fun updateProfileInfo() {
        // Updates the profile icon
        val uri = getSharedPrefString("profileIconURI")
        if (uri != null) {
            profileIcon.setPadding(0, 0, 0, 0)
            val mgr = ImageManager.create(this)
            mgr.loadImage(profileIcon, Uri.parse(uri))
        } else {
            val profileMargin = dpToPx(10)
            profileIcon.setPadding(profileMargin, profileMargin, profileMargin, profileMargin)
            profileIcon.setImageResource(R.drawable.ic_games_controller_black_40dp)
        }

        // Sets up the profile name
        val profileName = getSharedPrefString("profileName")
        if (profileName != null) {
            val name = profileName.split(" ")
            firstName.text = name[0]
            lastName.text = name[1]
        } else {
            firstName.text = getString(R.string.first_name_placeholder)
            lastName.text = getString(R.string.last_name_placeholder)
        }

        // Shows the logout button if the user is logged in
        if (profileName != null || uri != null) {
            animateShow(signOutButton)
        } else {
            animateHide(signOutButton)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == signInRequestCode) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            val signInAccount = result.signInAccount

            // Checks if the user has actually logged in
            if (result.isSuccess && signInAccount != null) {
                val info = Games.getPlayersClient(this, signInAccount)

                info.currentPlayer.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Saves the information about the user in the shared preferences
                        storeSharedPref("profileIconURI", task.result?.iconImageUri.toString())
                        storeSharedPref("profileName", task.result?.name.toString())

                        updateProfileInfo()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Updates the time when coming back from the game screen
        updateBestTime()
    }
}