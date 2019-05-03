package com.benjeau.wordsearch

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.collections.ArrayList
import android.R.attr.button
import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Handler
import androidx.constraintlayout.widget.ConstraintSet
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import android.animation.ValueAnimator.AnimatorUpdateListener




class GameActivity : AppCompatActivity() {

    private lateinit var letterTextViews: ArrayList<TextView>
    private lateinit var wordBankTextViews: ArrayList<TextView>
    private lateinit var wordBank: ArrayList<String>
    private lateinit var wordSearchletters: ArrayList<String>

    private lateinit var chronometer: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupGame()

        // Set dummy profile picture
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        Glide.with(this)
                .load("https://api.adorable.io/avatars/100/sdf")
                .apply(RequestOptions.circleCropTransform())
                .into(profileIcon)

        // Sets action for the home button
        val playGame: ImageButton = findViewById(R.id.homeIcon)
        playGame.setOnClickListener {
//            finish()
            finishedGame()
        }

        foundWord(1)

        val playAgainButton: Button = findViewById(R.id.playAgainButton)
        playAgainButton.setOnClickListener{
            playAgain()
        }

        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener{
            finish()
        }
    }

    private fun setupGame() {
        // Initialize arrays
        letterTextViews = arrayListOf()
        wordBankTextViews = arrayListOf()
        wordSearchletters = arrayListOf()

        wordBank = WORDBANK

        chronometer = findViewById(R.id.time)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        val letters: FlexboxLayout = findViewById(R.id.letters)
        letters.removeAllViews()

        val wordBankLayout: FlexboxLayout = findViewById(R.id.wordBank)
        wordBankLayout.removeAllViews()

        val score: TextView = findViewById(R.id.score)
        score.text = "0"

        // Populates the TextViews for the game
        val typeface = ResourcesCompat.getFont(this, R.font.actor)
        createLetters()
        populateWordBank(typeface)
        populateWordSearchBoard(typeface)
    }

    private fun createLetters() {
        // Adds letters randomly
        for (i in 0..99) {
            wordSearchletters.add("")
//            letters.add(ALPHABET[(0 until ALPHABET.length).random()].toString())
        }

        // Adds words
        wordBank.forEach {
            var interfere = false
            var lines = arrayListOf(0,1,2,3,4,5,6,7,8,9)
            var isHorizontal = (0..1).random() == 1
            var changedOrientation = false

            System.out.println(it)
            do {
                if (interfere) {
                    if (changedOrientation) {
                        changedOrientation = true
                        isHorizontal = !isHorizontal
                    } else {
                        changedOrientation = false
                        interfere = false
                        isHorizontal = !isHorizontal
                    }
                }

                val line = lines[(0 until lines.size).random()]
                val offset = (0..(10 - it.length)).random()

                for (i in 0 until it.length) {
                    var index = 0

                    if (isHorizontal) {
                        index = line * 10 + offset + i
                    } else {
                        index = offset * 10 + line + i * 10
                    }

                    if (wordSearchletters[index] != "" && wordSearchletters[index] != it[i].toString().toUpperCase()) {
                        interfere = true
                        break
                    }
                }

                if (!interfere) {
                    for (i in 0 until it.length) {
                        var index = 0

                        if (isHorizontal) {
                            index = line * 10 + offset + i
                        } else {
                            index = offset * 10 + line + i * 10
                        }

                        wordSearchletters[index] = it[i].toString().toUpperCase()
                    }
                }
            } while(interfere)
        }
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
        wordSearchletters.forEach {
            val text = TextView(this)
            letterTextViews.add(text)
            letters.addView(text)
            text.text = it
            text.setTextColor(resources.getColor(R.color.colorDarkGray))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.textSize = fontSize.toFloat()
            text.typeface = typeface
            text.gravity = Gravity.CENTER
            (text.layoutParams as FlexboxLayout.LayoutParams).flexBasisPercent = 0.0875f
        }
    }

    private fun playAgain() {
        setupGame()

        val gameBoard: CardView = findViewById(R.id.gameBoard)
        val colorFrom = resources.getColor(R.color.colorAccent)
        val colorTo = resources.getColor(R.color.white)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator -> gameBoard.setCardBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()

        val gameBoardContent: ConstraintLayout = findViewById(R.id.gameBoardContent)
        gameBoardContent.animate().alpha(1.0f).duration = 500
        gameBoardContent.visibility = View.VISIBLE

        val gameBoardFinished: ConstraintLayout = findViewById(R.id.gameBoardFinished)
        gameBoardFinished.visibility = View.GONE
        gameBoardFinished.animate().alpha(0.0f).duration = 500

        val gameInfo: CardView = findViewById(R.id.gameInfo)

        val anim = ValueAnimator.ofInt(0, 143)
        val topPadding = dpToPx(20)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = gameInfo.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.height = `val`
            layoutParams.setMargins(0, ((`val`.toFloat() / 143.0) * topPadding.toFloat()).toInt(), 0, 0)
            gameInfo.layoutParams = layoutParams
            gameInfo.visibility = View.VISIBLE
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override
            fun onAnimationEnd(animation: Animator) {
                val layoutParams = gameInfo.layoutParams
                layoutParams.height = 143
                gameInfo.layoutParams = layoutParams
            }
        })
        anim.duration = 500
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
    }

    private fun finishedGame() {
        chronometer.stop()

        val gameBoard: CardView = findViewById(R.id.gameBoard)
        val colorFrom = resources.getColor(R.color.white)
        val colorTo = resources.getColor(R.color.colorAccent)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator -> gameBoard.setCardBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()

        val gameBoardContent: ConstraintLayout = findViewById(R.id.gameBoardContent)
        gameBoardContent.animate().alpha(0.0f)
        gameBoardContent.visibility = View.GONE

        val gameBoardFinished: ConstraintLayout = findViewById(R.id.gameBoardFinished)
        gameBoardFinished.visibility = View.VISIBLE
        gameBoardFinished.animate().alpha(1.0f)

        val gameInfo: CardView = findViewById(R.id.gameInfo)

        val anim = ValueAnimator.ofInt(gameInfo.measuredHeight, 0)
        val topPadding = (gameInfo.layoutParams as ConstraintLayout.LayoutParams).topMargin
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = gameInfo.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.height = `val`
            layoutParams.setMargins(0, ((`val`.toFloat() / 143.0) * topPadding.toFloat()).toInt(), 0, 0)
            gameInfo.layoutParams = layoutParams
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override
            fun onAnimationEnd(animation: Animator) {
                val layoutParams = gameInfo.layoutParams
                layoutParams.height = 0
                gameInfo.layoutParams = layoutParams
                gameInfo.visibility = View.GONE
            }
        })
        anim.duration = 500
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
    }

    private fun foundWord(wordIndex: Int) {
        val score: TextView = findViewById(R.id.score)
        score.text = (score.text.toString().toInt() + 1).toString()
        wordBankTextViews[wordIndex].paintFlags = wordBankTextViews[wordIndex].paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    private fun handleTouch() {
        
    }

    private fun dpToPx(dp: Int): Int {
        return if (dp < 0) dp else Math.round(dp * this.resources.displayMetrics.density)
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val WORDBANK = arrayListOf("Swift", "ObjectiveC", "Java", "Kotlin", "Variable", "Mobile")
    }
}