package com.benjeau.wordsearch

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val playGame: Button = findViewById(R.id.playGame)
        playGame.setOnClickListener{
            val myIntent = Intent(this, GameActivity::class.java)
            startActivity(myIntent)
        }
    }
}
