package com.benjeau.wordsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.app.AlertDialog
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.games.Games
import com.google.android.gms.games.Player

class HomeActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPref = SharedPreferences(this)
        val highScoreText: TextView = findViewById(R.id.highScore)
        highScoreText.text = if (sharedPref.getValueString("bestTime") == null) "N.A." else sharedPref.getValueString("bestTime") + " s."

        val playGame: Button = findViewById(R.id.playGame)
        playGame.setOnClickListener{
            val myIntent = Intent(this, GameActivity::class.java)
            startActivity(myIntent)
        }

        val googlePlayIcon: ImageButton = findViewById(R.id.googlePlayButton)
        googlePlayIcon.setOnClickListener{
            val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
            val signInClient = GoogleSignIn.getClient(this, signInOptions)
            startActivityForResult(signInClient.signInIntent, 5)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            System.out.println(resultCode)
            if (result.isSuccess) {
                // The signed in account is stored in the result.
                val signedInAccount = result.signInAccount
                if (signedInAccount != null) {
                    val info = Games.getPlayersClient(this, signedInAccount)
                    info.currentPlayer.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val player: Player? = task.result

                            sharedPref.store("profileIconURI", player?.iconImageUri.toString())
                            sharedPref.store("profileName", player?.name.toString())
                        }
                    }
                }
            } else {
                var message = result.status.statusMessage
                System.out.println("Message " + result.status)
                if (message == null || message.isEmpty()) {
                    message = "ERROR"
                }
                AlertDialog.Builder(this).setMessage(message)
                    .setNeutralButton(android.R.string.ok, null).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val highScoreText: TextView = findViewById(R.id.highScore)
        highScoreText.text = if (sharedPref.getValueString("bestTime") == null) "N.A." else sharedPref.getValueString("bestTime") + " s."
    }
}
