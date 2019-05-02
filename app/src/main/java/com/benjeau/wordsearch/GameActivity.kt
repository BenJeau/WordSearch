package com.benjeau.wordsearch

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout

class GameActivity : AppCompatActivity() {

    private lateinit var letterTextViews: ArrayList<TextView>
    private lateinit var wordBankTextViews: ArrayList<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Initialize arrays
        letterTextViews = arrayListOf()
        wordBankTextViews = arrayListOf()

        // Set dummy profile picture
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        Glide.with(this)
                .load("https://api.adorable.io/avatars/100/sdf")
                .apply(RequestOptions.circleCropTransform())
                .into(profileIcon)

        // Sets action for the home button
        val playGame: ImageButton = findViewById(R.id.homeIcon)
        playGame.setOnClickListener { finish() }

        // Populates the TextViews for the game
        val typeface = ResourcesCompat.getFont(this, R.font.actor)
        populateWordBank(typeface)
        populateWordSearchBoard(typeface)
    }

    private fun populateWordBank(typeface: Typeface?) {
        val wordBankLayout: FlexboxLayout = findViewById(R.id.wordBank)
        val padding = dpToPx(5)
        val fontSize = dpToPx(8)
        wordBank.forEach {
            val text = TextView(this)
            text.text = it
            wordBankTextViews.add(text)
            wordBankLayout.addView(text)
            text.setTextColor(resources.getColor(R.color.white))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.setPadding(padding, padding, padding, padding)
            text.textSize = fontSize.toFloat()
            text.typeface = typeface
            (text.layoutParams as FlexboxLayout.LayoutParams).flexBasisPercent = 0.3f
        }
    }

    private fun populateWordSearchBoard(typeface: Typeface?) {
        val letters: FlexboxLayout = findViewById(R.id.letters)
        val fontSize = dpToPx(10)
        for (i in 0..98) {
            val letter = ALPHABET[(0 until ALPHABET.length).random()].toString()
            val text = TextView(this)
            letterTextViews.add(text)
            letters.addView(text)
            text.text = letter
            text.setTextColor(resources.getColor(R.color.colorDarkGray))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.textSize = fontSize.toFloat()
            text.typeface = typeface
            text.gravity = Gravity.CENTER
            (text.layoutParams as FlexboxLayout.LayoutParams).flexBasisPercent = 0.09f
        }
    }

    private fun dpToPx(dp: Int): Int {
        return if (dp < 0) dp else Math.round(dp * this.resources.displayMetrics.density)
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val wordBank = arrayListOf("Swift", "ObjectiveC", "Java", "Kotlin", "Variable", "Mobile")
    }
}