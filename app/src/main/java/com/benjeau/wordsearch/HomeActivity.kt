package com.benjeau.wordsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.TextView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sharedPref = SharedPreferences(this)
        val highScoreText: TextView = findViewById(R.id.highScore)
        highScoreText.text = if (sharedPref.getValueString("bestTime") == null) "N.A." else sharedPref.getValueString("bestTime") + " s."

        val playGame: Button = findViewById(R.id.playGame)
        playGame.setOnClickListener{
            val myIntent = Intent(this, GameActivity::class.java)
            startActivity(myIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = SharedPreferences(this)
        val highScoreText: TextView = findViewById(R.id.highScore)
        highScoreText.text = if (sharedPref.getValueString("bestTime") == null) "N.A." else sharedPref.getValueString("bestTime") + " s."
    }
}
