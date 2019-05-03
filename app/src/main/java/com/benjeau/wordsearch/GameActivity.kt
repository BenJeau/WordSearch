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
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import android.widget.Toast
import android.view.MotionEvent
import java.util.*


class GameActivity : AppCompatActivity() {

    private lateinit var letterTextViews: ArrayList<TextView>
    private lateinit var wordBankTextViews: ArrayList<TextView>
    private lateinit var wordBank: ArrayList<String>
    private lateinit var wordBankFound: ArrayList<Boolean>
    private lateinit var wordSearchletters: ArrayList<String>
    private lateinit var wordBankSearch: MutableMap<String, ArrayList<Int>>
    private lateinit var letterStates: ArrayList<Int>

    private lateinit var chronometer: Chronometer

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val x = event.x
        val y = event.y
        Toast.makeText(
            this, "x=$x y=$y",
            Toast.LENGTH_SHORT
        ).show()
        return false
    }

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
        playGame.setOnClickListener { finish() }

        val playAgainButton: Button = findViewById(R.id.playAgainButton)
        playAgainButton.setOnClickListener {
            playAgain()
        }

        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun setupGame() {
        // Initialize arrays
        letterTextViews = arrayListOf()
        wordBankTextViews = arrayListOf()
        wordSearchletters = arrayListOf()
        wordBankSearch = mutableMapOf()
        letterStates = arrayListOf()
        wordBankFound = arrayListOf()

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
        }

        // Adds words
        wordBank.forEach {
            var interfere = false
            val lines = arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            var isHorizontal = (0..1).random() == 1
            var changedOrientation = false

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
                    wordBankSearch[it] = arrayListOf()
                    for (i in 0 until it.length) {
                        var index = 0

                        if (isHorizontal) {
                            index = line * 10 + offset + i
                        } else {
                            index = offset * 10 + line + i * 10
                        }

                        wordBankSearch[it]?.add(index)
                        wordSearchletters[index] = it[i].toString().toUpperCase()
                    }
                }
            } while (interfere)
        }

        for (i in 0..99) {
            if (wordSearchletters[i] == "")
                wordSearchletters[i] = (ALPHABET[(0 until ALPHABET.length).random()].toString())
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
            wordBankFound.add(false)
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
        wordSearchletters.forEachIndexed { index, letter ->
            val text = TextView(this)
            letterTextViews.add(text)
            letters.addView(text)
            letterStates.add(0)
            text.text = letter
            text.setTextColor(resources.getColor(R.color.colorDarkGray))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.textSize = fontSize.toFloat()
            text.typeface = typeface
            text.gravity = Gravity.CENTER
            text.setOnClickListener { v ->
                var changeState = true

                if (letterStates.contains(1) || letterStates.contains(3)) {
                    val numSelected = letterStates.count { it == 1 || it == 3 }

                    if (numSelected == 1) {
                        when (val letterIndex = if (letterStates.indexOf(1) > 0) letterStates.indexOf(1) else letterStates.indexOf(3)) {
                            // Checks if clicked on the same letter
                            index -> changeState = true
                            // Checks the top left corner
                            0 -> changeState = index == letterIndex + 1 || index == letterIndex + 10
                            // Checks the top right corner
                            9 -> changeState = index == letterIndex - 1 || index == letterIndex + 10
                            // Checks the bottom left corner
                            90 -> changeState = index == letterIndex + 1 || index == letterIndex - 10
                            // Checks the bottom right corner
                            99 -> changeState = index == letterIndex - 1 || index == letterIndex - 10
                            // Checks the top
                            in 1..8 -> changeState = index == letterIndex + 1 || index == letterIndex - 1 || index == letterIndex + 10
                            // Checks the bottom
                            in 91..98 -> changeState = index == letterIndex + 1 || index == letterIndex - 1 || index == letterIndex - 10
                            // Checks the left
                            in 10 until 99 step 10 -> changeState = index == letterIndex + 1 || index == letterIndex + 10 || index == letterIndex - 10
                            // Checks the right
                            in 9 until 99 step 10 -> changeState = index == letterIndex - 1 || index == letterIndex + 10 || index == letterIndex - 10
                            else -> changeState = index == letterIndex - 1 || index == letterIndex + 1 || index == letterIndex + 10 || index == letterIndex - 10
                        }
                    } else {
                        val let = letterStates.withIndex().filter { it.value == 1 || it.value == 3 }. map { it.index }
                        val isHorizontal = let[0] in let[1]-1..let[1]+1
                        val line: Int = if (isHorizontal) Math.floor(let[0] / 10.toDouble()).toInt() else let[0] % 10
                        val firstOffset: Int = if (!isHorizontal) Math.floor(let.first() / 10.toDouble()).toInt() else let.first() % 10
                        val lastOffset: Int = if (!isHorizontal) Math.floor(let.last() / 10.toDouble()).toInt() else let.last() % 10
                        val currentOffset: Int = if (!isHorizontal) Math.floor(index / 10.toDouble()).toInt() else index % 10

                        // Restricts the user to only select letters from the same line
                        val sameLine = (if (isHorizontal) Math.floor(index / 10.toDouble()).toInt() else index % 10) == line
                        val before = currentOffset == firstOffset - 1
                        val after = currentOffset == lastOffset +1
                        changeState = sameLine && ( before|| after) || letterStates[index] == 1|| letterStates[index] == 3
                    }
                }

                if (changeState) {
                    if (letterStates[index] == 0) {
                        v.setBackgroundResource(R.drawable.letter_select)
                        letterStates[index]++
                    } else if (letterStates[index] == 2) {
                        v.setBackgroundResource(R.drawable.letter_select_found)
                        letterStates[index]++
                    } else {
                        if (letterStates[index] == 1) {
                            v.setBackgroundResource(0)
                        } else {
                            v.setBackgroundResource(R.drawable.letter_found)
                        }
                        letterStates[index]--
                    }
                }


                val let = letterStates.withIndex().filter { it.value == 1 || it.value == 3 }. map { it.index }
                val foundshit = wordBankSearch.filterValues { Arrays.equals(it.toIntArray(), let.toIntArray()) }
                if (foundshit.isNotEmpty()) {
                    foundWord(wordBank.indexOf(foundshit.keys.first()))
                }
            }
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
            layoutParams.setMargins(
                0,
                ((`val`.toFloat() / 143.0) * topPadding.toFloat()).toInt(),
                0,
                0
            )
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

        val elapsedSeconds = (SystemClock.elapsedRealtime() - chronometer.base) / 1000

        val finishDescription: TextView = findViewById(R.id.finishDescription)
        finishDescription.text = "Completed the game in $elapsedSeconds seconds!"
        
        val sharedPref = SharedPreferences(this)
        var bestTime = sharedPref.getValueString("bestTime") ?: ""
        if (bestTime == "" || bestTime.toInt() > elapsedSeconds.toInt()) {
            bestTime = elapsedSeconds.toString()
        }
        sharedPref.store("bestTime", bestTime)

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
            layoutParams.setMargins(
                0,
                ((`val`.toFloat() / 143.0) * topPadding.toFloat()).toInt(),
                0,
                0
            )
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
        wordBankTextViews[wordIndex].paintFlags =
            wordBankTextViews[wordIndex].paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        wordBankFound[wordIndex] = true
        wordBankSearch[wordBank[wordIndex]]?.forEach {
            letterStates[it] = 2
            letterTextViews[it].setBackgroundResource(R.drawable.letter_found)
            letterTextViews[it].setTextColor(resources.getColor(R.color.white))
        }

        if (!wordBankFound.contains(false)) {
            finishedGame()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return if (dp < 0) dp else Math.round(dp * this.resources.displayMetrics.density)
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val WORDBANK =
            arrayListOf("Swift", "ObjectiveC", "Java", "Kotlin", "Variable", "Mobile")
    }
}