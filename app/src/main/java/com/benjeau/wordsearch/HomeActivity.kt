package com.benjeau.wordsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.tasks.OnCompleteListener

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

        // Creates the values for the dropdown
        val dropdown: Spinner = findViewById(R.id.spinner)
        val items = (6..10).toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                storeSharedPref(numberOfWords, items[position])
            }
        }

        // Sets the default value for the spinner/dropdown
        if (getSharedPrefInt(numberOfWords) != -1) {
            dropdown.setSelection(items.indexOf(getSharedPrefInt(numberOfWords)))
        } else {
            dropdown.setSelection(2)
        }

        signOutButton.setOnClickListener {
            googlePlaySignOut(OnCompleteListener {
                storeSharedPref(profileIconURI, null)
                storeSharedPref(profileName, null)
                updateProfileInfo()
            })
        }
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
        bestTimeText.text = if (getSharedPrefString(bestTimeValue) == null) {
            "N.A."
        } else {
            getSharedPrefString(bestTimeValue) + " s."
        }
    }

    /**
     * Puts the information about the signed in user, if the user is signed in Google Play Games
     */
    private fun updateProfileInfo() {
        updateProfileView(profileIcon, firstName, lastName)

        // Shows the logout button if the user is logged in
        if (getSharedPrefString(profileName) != null || getSharedPrefString(profileIconURI) != null) {
            animateShow(signOutButton)
        } else {
            animateHide(signOutButton)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == signInRequestCode) {
            handleSignInRequest(data, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Saves the information about the user in the shared preferences
                    storeSharedPref(profileIconURI, task.result?.iconImageUri.toString())
                    storeSharedPref(profileName, task.result?.name.toString())

                    updateProfileInfo()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        // Updates the time and profile information when coming back from the game screen
        updateBestTime()
        updateProfileInfo()
    }
}