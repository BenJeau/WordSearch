package com.benjeau.wordsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import android.util.TypedValue
import android.view.ViewTreeObserver
import android.graphics.Typeface
import android.view.Gravity
import androidx.core.content.res.ResourcesCompat


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

        val wordBankLayout: FlexboxLayout = findViewById(R.id.wordBank)

        val wordBank = arrayListOf("Swift", "ObjectiveC", "Java", "Kotlin", "Variable", "Mobile")
        val wordBankText = arrayListOf<TextView>()


        val padding = convertDpToPx(5f)
        val fontSize = convertDpToPx(10f)
        val fontSizeTop = convertDpToPx(8f)


        wordBank.forEach{
            val text = TextView(this)
            wordBankText.add(text)
            wordBankLayout.addView(text)
            text.text = it
            text.setTextColor(resources.getColor(R.color.white))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.setPadding(padding, padding, padding, padding)
            val face = ResourcesCompat.getFont(this, R.font.actor)
            text.textSize = fontSizeTop.toFloat()
            text.typeface = face
        }

        wordBankLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                wordBankLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                wordBankText.forEach{it.width = wordBankLayout.width/3}
            }
        })


        val letters: FlexboxLayout = findViewById(R.id.letters)
        val lettersText = arrayListOf<TextView>()
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        for (i in 0..98) {
            val letter = alphabet[(0 until alphabet.length).random()].toString()
            val text = TextView(this)
            lettersText.add(text)
            letters.addView(text)
            text.text = letter
            text.setTextColor(resources.getColor(R.color.colorDarkGray))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            val face = ResourcesCompat.getFont(this, R.font.actor)
            text.textSize = fontSize.toFloat()
            text.typeface = face
            text.gravity = Gravity.CENTER
        }

        letters.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                letters.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val paddingLetters = convertDpToPx(40f)
                lettersText.forEach{
                    it.width = (letters.width-paddingLetters)/10
                    it.height = (letters.height-paddingLetters)/10
                }
            }
        })
    }

    private fun convertDpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.displayMetrics
        ).toInt()
    }
}
