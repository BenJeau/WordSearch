package com.benjeau.wordsearch

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val profileIcon: ImageView = findViewById(R.id.profileIcon)

        Glide.with(this)
            .load("https://api.adorable.io/avatars/100/sdf")
            .apply(RequestOptions.circleCropTransform())
            .into(profileIcon)

        val playGame: ImageButton = findViewById(R.id.homeIcon)
        playGame.setOnClickListener{finish()}
    }
}
