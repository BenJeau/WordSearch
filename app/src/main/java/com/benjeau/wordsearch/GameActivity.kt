package com.benjeau.wordsearch

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.flexbox.FlexboxLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.collections.ArrayList
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.content.Intent
import android.os.SystemClock
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.util.*
import com.google.android.gms.games.Games
import com.google.android.gms.tasks.OnCompleteListener

class GameActivity : AppCompatActivity() {

    /**
     * The words in the word bank
     */
    private lateinit var wordBank: ArrayList<String>

    /**
     * The state of the words of the word bank, the value is true if they have been found, false
     * otherwise
     */
    private lateinit var wordBankFound: ArrayList<Boolean>

    /**
     * The ArrayList of letters that are displayed in the board game
     */
    private lateinit var wordSearchLetters: ArrayList<String>

    /**
     * The link between the words of the word bank and indexes of their letters in the board
     */
    private lateinit var wordBankSearch: HashMap<String, ArrayList<Int>>

    /**
     * The state of the letters. The numbers represents
     *      0 -> untouched
     *      1 -> touched
     *      2 -> untouched and part of a word found
     *      3 -> touched and part of a word found
     */
    private lateinit var letterStates: ArrayList<Int>

    /**
     * References to the views in the layout
     */
    private lateinit var chronometer: Chronometer
    private lateinit var letters: FlexboxLayout
    private lateinit var wordBankLayout: FlexboxLayout
    private lateinit var score: TextView
    private lateinit var gameBoardContent: ConstraintLayout
    private lateinit var gameBoardFinished: ConstraintLayout
    private lateinit var gameInfo: CardView
    private lateinit var gameBoard: CardView
    private lateinit var profileIcon: ImageView
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView

    /**
     * The font used for the TextViews
     */
    private var typeface: Typeface? = null

    private var gameInfoHeight: Int? = null
    private var elapsedSeconds: Int = -1
    private var wordBankTextColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Restores the data of view after rotation
        if (savedInstanceState != null) {
            wordBank = savedInstanceState.getStringArrayList("wordBank") as ArrayList
            wordSearchLetters = savedInstanceState.getStringArrayList("wordSearchLetters") as ArrayList
            wordBankSearch = savedInstanceState.getSerializable("wordBankSearch") as HashMap<String, ArrayList<Int>>
            letterStates = savedInstanceState.getIntegerArrayList("letterStates") as ArrayList
            wordBankFound = savedInstanceState.getBooleanArray("wordBankFound")?.toCollection(ArrayList()) ?: arrayListOf()
            elapsedSeconds = savedInstanceState.getInt("elapsedSeconds")
        }

        initialSetup(savedInstanceState != null)

        // Properly sets up the view according to the data before rotation
        if (savedInstanceState != null) {
            // Sets the score and chronometer
            score.text = savedInstanceState.getString("score")
            chronometer.base = savedInstanceState.getLong("chronometerBase")
            chronometer.start()

            // Strikethrough the found words
            wordBankFound.forEachIndexed {index, i ->
                if (i) {
                    val child = wordBankLayout.getChildAt(index) as TextView
                    child.paintFlags = child.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

            // Checks if the game is finished
            if (!wordBankFound.contains(false)) {
                gameInfo.visibility = View.GONE

                gameBoardContent.visibility = View.GONE
                gameBoardContent.alpha = 0f

                gameBoardFinished.visibility = View.VISIBLE
                gameBoardFinished.alpha = 1f

                gameBoard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))

                val finishDescription: TextView = findViewById(R.id.finishDescription)
                finishDescription.text = String.format(resources.getString(R.string.finished_message), elapsedSeconds)
            } else {
                // Restores the view of the letters' background and text color
                letterStates.forEachIndexed { index, i ->
                    when(i) {
                        1 -> letters.getChildAt(index).setBackgroundResource(R.drawable.letter_select)
                        2 -> {
                            letters.getChildAt(index).setBackgroundResource(R.drawable.letter_found)
                            (letters.getChildAt(index) as TextView).setTextColor(ContextCompat.getColor(this, R.color.white))
                        }
                        3 -> {
                            letters.getChildAt(index).setBackgroundResource(R.drawable.letter_select_found)
                            (letters.getChildAt(index) as TextView).setTextColor(ContextCompat.getColor(this, R.color.white))
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        // Saves the state of the view before rotation
        outState?.putIntegerArrayList("letterStates", letterStates)
        outState?.putStringArrayList("wordSearchLetters", wordSearchLetters)
        outState?.putBooleanArray("wordBankFound", wordBankFound.toBooleanArray())
        outState?.putStringArrayList("wordBank", wordBank)
        outState?.putSerializable("wordBankSearch", wordBankSearch)
        outState?.putLong("chronometerBase", chronometer.base)
        outState?.putString("score", score.text.toString())
        outState?.putInt("elapsedSeconds", elapsedSeconds)
    }

    /**
     * Performs the initial setup of the game
     */
    private fun initialSetup(rotated: Boolean) {
        setupViews()

        updateProfileView(profileIcon, firstName, lastName)

        // Shows prompt to sign in Google Play Games
        val profileLayout: ConstraintLayout = findViewById(R.id.profilePictureLayout)
        profileLayout.setOnClickListener {
            profileOnClick()
        }

        // Gets the height of the gameInfo layout for animating it
        gameInfo.post {
            gameInfo.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            gameInfoHeight = gameInfo.measuredHeight
        }

        // Defines the typeface used for the TextViews programmatically inserted
        typeface = ResourcesCompat.getFont(this, R.font.actor)

        // Shows an alert and allows the user to restart the game
        val playAgainIcon: ImageButton = findViewById(R.id.playAgainIcon)
        playAgainIcon.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Create New Game")
                .setMessage("You will lose all of your progress")
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    setupGame(false)
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }

        // Sets action for the home button
        val homeIcon: ImageButton = findViewById(R.id.homeIcon)
        homeIcon.setOnClickListener { finish() }

        // Sets action for the buttons when ending the game
        val playAgainButton: Button = findViewById(R.id.playAgainButton)
        val exitButton: Button = findViewById(R.id.exitButton)
        playAgainButton.setOnClickListener { playAgain() }
        exitButton.setOnClickListener { finish() }

        setupGame(rotated)
    }

    /**
     * Create references to the views often used
     */
    private fun setupViews() {
        letters = findViewById(R.id.letters)
        wordBankLayout = findViewById(R.id.wordBank)
        chronometer = findViewById(R.id.time)
        score = findViewById(R.id.score)
        gameBoardContent = findViewById(R.id.gameBoardContent)
        gameBoardFinished = findViewById(R.id.gameBoardFinished)
        gameInfo = findViewById(R.id.gameInfo)
        gameBoard = findViewById(R.id.gameBoard)
        profileIcon = findViewById(R.id.profileIcon)
        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)

        // Allows the achievements pop-ups to show
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val gamesClient = Games.getGamesClient(this@GameActivity, account)
            gamesClient.setViewForPopups(findViewById(R.id.gps_popup))
        }
    }

    /**
     * Sets up a new game
     */
    private fun setupGame(rotated: Boolean) {
        if (!rotated) {
            // Initializes the arrays
            wordBankSearch = hashMapOf()
            wordBankFound = arrayListOf()
            wordBank = arrayListOf()

            // Clears the game information and start the chronometer
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
            score.text = "0"

            // Clears the views to add the new words and letters
            letters.removeAllViews()
            wordBankLayout.removeAllViews()

            // Generates the words and letters for the upcoming game
            generateWordBank()
            createLetters()
        }

        // Populates the TextViews for the game
        populateWordBank(typeface)
        populateWordSearchBoard(typeface)
    }

    /**
     * Adds every mandatory words and random optional words (depending on user input) to the word bank
     */
    private fun generateWordBank() {
        wordBank.addAll(WORDS)

        val numOfWords = getSharedPrefInt(numberOfWords)

        if (numOfWords != -1 || numOfWords > 6) {
            val numRandom = numOfWords - 6
            val randomIndexes = arrayListOf<Int>()

            for (i in (0 until numRandom)) {
                var random: Int
                do {
                    random = (0 until OPTIONAL_WORDS.size).random()
                } while(random in randomIndexes)

                randomIndexes.add(random)
                wordBank.add(OPTIONAL_WORDS[random])
            }
        }

        wordBank.sortByDescending {it.length}

        repeat(wordBank.size) { wordBankFound.add(false) }
    }

    /**
     * Create the array of letters representing the game board
     *
     * Note: If I would have more time, I would create a better algorithm which relies less on
     *     on the randomness of the location of the words
     */
    private fun createLetters() {
        wordSearchLetters = arrayListOf()
        letterStates = arrayListOf()
        var restartCreation = false

        // Initializes the array with empty strings and their state
        for (i in 0..99) {
            wordSearchLetters.add("")
            letterStates.add(0)
        }

        // Adds words in the list of letters
        wordBank.forEach {
            val lines = (0..9).toMutableList()
            var isHorizontal = (0..1).random() == 1
            var changedOrientation = false
            var interfere = false

            // Tries to find a place where the word will not interfere with other existing words
            do {

                // If it interferes, change the orientation
                if (interfere) {
                    if (changedOrientation) {
                        changedOrientation = false
                        interfere = false
                        isHorizontal = !isHorizontal
                    } else {
                        changedOrientation = true
                        interfere = false
                        isHorizontal = !isHorizontal
                    }
                }

                // Get a random line and offset from the start of the line
                val line = lines[(0 until lines.size).random()]
                val offset = (0..(10 - it.length)).random()

                // Checks if it will interfere
                for (i in 0 until it.length) {
                    val index = if (isHorizontal) {
                        line * 10 + offset + i
                    } else {
                        offset * 10 + line + i * 10
                    }

                    if (wordSearchLetters[index] != "" && wordSearchLetters[index] != it[i].toString().toUpperCase()) {
                        interfere = true
                        if (changedOrientation) {
                            lines.remove(line)
                            if (lines.size == 0) {
                                restartCreation = true
                                return@forEach
                            }
                        }
                        break
                    }
                }

                // Adds it to the array of letters if it doesn't interfere
                if (!interfere) {
                    wordBankSearch[it] = arrayListOf()
                    for (i in 0 until it.length) {
                        val index = if (isHorizontal) {
                            line * 10 + offset + i
                        } else {
                            offset * 10 + line + i * 10
                        }

                        wordBankSearch[it]?.add(index)
                        wordSearchLetters[index] = it[i].toString().toUpperCase()
                    }
                }
            } while (interfere)
        }

        // If the is no place to put the other letters, try again
        if (restartCreation) {
            createLetters()
            return
        }

        // Adds random letters to the spaces that are empty
        for (i in 0..99) {
            if (wordSearchLetters[i] == "") {
                wordSearchLetters[i] = (ALPHABET[(0 until ALPHABET.length).random()].toString())
            }
        }

        // Shuffles the word bank to make it look different each time
        wordBank.shuffle()
    }

    /**
     * Populates the words in the layout of the word search bank
     */
    private fun populateWordBank(typeface: Typeface?) {
        val padding = spToPx(7f)
        val fontSize = spToPx(8f).toFloat()
        val textColor = wordBankTextColor ?: ContextCompat.getColor(this, R.color.white)

        wordBank.forEach {
            val text = TextView(this)
            text.text = it
            wordBankLayout.addView(text)
            text.setTextColor(textColor)
            text.setPadding(padding, padding, padding, padding)
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.textSize = fontSize
            text.typeface = typeface
            text.gravity = Gravity.CENTER
        }
    }

    /**
     * Populates the letters in the layout of the word search board
     */
    private fun populateWordSearchBoard(typeface: Typeface?) {
        val fontSize = spToPx(9f).toFloat()
        val textColor = ContextCompat.getColor(this, R.color.colorDarkGray)

        wordSearchLetters.forEachIndexed { index, letter ->
            val text = TextView(this)
            letters.addView(text)
            text.text = letter
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.textSize = fontSize
            text.typeface = typeface
            text.gravity = Gravity.CENTER
            text.setTextColor(textColor)
            text.setOnTouchListener { _, event ->
                letterOnTouch(index, text, event)
                true
            }
            (text.layoutParams as FlexboxLayout.LayoutParams).flexBasisPercent = 0.0875f
        }
    }

    /**
     * The onTouchListener for the letters' TextViews dealing with the tapping and swiping over the
     * letters
     */
    private fun letterOnTouch(index: Int, view: TextView, event: MotionEvent) {
        val x = event.x
        val y = event.y
        val height = view.height
        val width = view.width
        val inXRange = x.toInt() in (0..width)
        val inYRange = y.toInt() in (0..height)

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                letterOnClick(index, view)
            }
            MotionEvent.ACTION_UP -> {
                checkIfWordSelected()
            }
            MotionEvent.ACTION_MOVE -> {
                if (inYRange && !inXRange || !inYRange && inXRange) {
                    val xoff = Math.floor((x / width).toDouble()).toInt()
                    val yoff = Math.floor((y / height).toDouble()).toInt()

                    letterStates.forEachIndexed { letterIndex, i ->
                        if (i == 1 || i == 3) {
                            letterStates[letterIndex]--

                            if (i == 1) {
                                letters.getChildAt(letterIndex).setBackgroundResource(0)
                            } else {
                                letters.getChildAt(letterIndex).setBackgroundResource(R.drawable.letter_found)
                            }
                        }
                    }
                    if (inYRange){
                        val indexesToChange: ArrayList<Int> = arrayListOf()
                        val yLine = Math.floor(index / 10.toDouble())

                        if (xoff < 0) {
                            (xoff..0).forEach{
                                val newIndex = index + it
                                val newYLine = Math.floor(newIndex / 10.toDouble())
                                if (newIndex in (0..99) && yLine == newYLine) indexesToChange.add(newIndex)
                            }
                        } else {
                            (0..xoff).forEach{
                                val newIndex = index + it
                                val newYLine = Math.floor(newIndex / 10.toDouble())
                                if (newIndex in (0..99) && yLine == newYLine) indexesToChange.add(newIndex)
                            }
                        }

                        indexesToChange.forEach {
                            if (letterStates[it] == 0) {
                                letters.getChildAt(it).setBackgroundResource(R.drawable.letter_select)
                                letterStates[it] = 1
                            } else if (letterStates[it] == 2){
                                letters.getChildAt(it).setBackgroundResource(R.drawable.letter_select_found)
                                letterStates[it] = 3
                            }
                        }
                    } else {
                        val indexesToChange: ArrayList<Int> = arrayListOf()

                        if (yoff < 0) {
                            (yoff .. 0).forEach{
                                val newIndex = index + 10 * it
                                if (newIndex in (0..99)) indexesToChange.add(newIndex)
                            }
                        } else {
                            (0 .. yoff).forEach{
                                val newIndex = index + 10 * it
                                if (newIndex in (0..99)) indexesToChange.add(newIndex)
                            }
                        }

                        indexesToChange.forEach {
                            if (letterStates[it] == 0) {
                                letters.getChildAt(it).setBackgroundResource(R.drawable.letter_select)
                                letterStates[it] = 1
                            } else if (letterStates[it] == 2){
                                letters.getChildAt(it).setBackgroundResource(R.drawable.letter_select_found)
                                letterStates[it] = 3
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The onClick callback for the letters' TextView
     *
     * @param index The index of the letter in the array of letters
     * @param view The letter's TextView
     */
    private fun letterOnClick(index: Int, view: TextView) {
        // Decides if the state of the letter should change
        var changeState = true

        // Checks if the letter is selected
        if (letterStates.contains(1) || letterStates.contains(3)) {
            val numSelected = letterStates.count { it == 1 || it == 3 }

            // If there's one letter selected, restrict the user to select only letters besides it.
            // Else, let the player select letters only in the line that he started and let the
            // player only select letters before and after the selected letters in the line
            if (numSelected == 1) {
                val letterIndex = if (letterStates.indexOf(1) >= 0) letterStates.indexOf(1) else letterStates.indexOf(3)
                val isTop = index == letterIndex - 10
                val isBottom = index == letterIndex + 10
                val isLeft = index == letterIndex - 1
                val isRight = index == letterIndex + 1
                when (letterIndex) {
                    // Checks if clicked on the same letter
                    index -> changeState = true
                    // Checks the top left corner
                    0 -> changeState = isRight || isBottom
                    // Checks the top right corner
                    9 -> changeState = isLeft || isBottom
                    // Checks the bottom left corner
                    90 -> changeState = isRight || isTop
                    // Checks the bottom right corner
                    99 -> changeState = isLeft || isTop
                    // Checks the top
                    in 1..8 -> changeState = isRight || isLeft || isBottom
                    // Checks the bottom
                    in 91..98 -> changeState = isRight || isLeft || isTop
                    // Checks the left
                    in 10 until 99 step 10 -> changeState = isRight || isBottom || isTop
                    // Checks the right
                    in 9 until 99 step 10 -> changeState = isLeft || isBottom || isTop
                    else -> changeState = isLeft || isRight || isBottom || isTop
                }
            } else {
                val selectedIndexes = letterStates.withIndex().filter { it.value == 1 || it.value == 3 }. map { it.index }
                val isHorizontal = selectedIndexes[0] in selectedIndexes[1]-1..selectedIndexes[1]+1
                val lineNumber: Int = if (isHorizontal) Math.floor(selectedIndexes[0] / 10.toDouble()).toInt() else selectedIndexes[0] % 10
                val firstOffset: Int = if (!isHorizontal) Math.floor(selectedIndexes.first() / 10.toDouble()).toInt() else selectedIndexes.first() % 10
                val lastOffset: Int = if (!isHorizontal) Math.floor(selectedIndexes.last() / 10.toDouble()).toInt() else selectedIndexes.last() % 10
                val currentOffset: Int = if (!isHorizontal) Math.floor(index / 10.toDouble()).toInt() else index % 10

                // Restricts the user to only select letters from the same line
                val sameLine = (if (isHorizontal) Math.floor(index / 10.toDouble()).toInt() else index % 10) == lineNumber
                val before = currentOffset == firstOffset - 1
                val after = currentOffset == lastOffset + 1
                changeState = sameLine && ( before|| after) || letterStates[index] == 1|| letterStates[index] == 3
            }
        }

        // Removes every other letter selected if the selected letter does not respect the rules
        // described above
        if (!changeState) {
            letterStates.forEachIndexed { letterIndex, i ->
                if (i == 1 || i == 3) {
                    letterStates[letterIndex]--

                    if (i == 1) {
                        letters.getChildAt(letterIndex).setBackgroundResource(0)
                    } else {
                        letters.getChildAt(letterIndex).setBackgroundResource(R.drawable.letter_found)
                    }
                }
            }
        }

        // Updates the view of the letter according its previous state and updates the state
        when {
            letterStates[index] == 0 -> {
                view.setBackgroundResource(R.drawable.letter_select)
                letterStates[index]++
            }
            letterStates[index] == 2 -> {
                view.setBackgroundResource(R.drawable.letter_select_found)
                letterStates[index]++
            }
            else -> {
                if (letterStates[index] == 1) {
                    view.setBackgroundResource(0)
                } else {
                    view.setBackgroundResource(R.drawable.letter_found)
                }
                letterStates[index]--
            }
        }

        checkIfWordSelected()
    }

    /**
     * Verifies if the user has selected a word, if so call foundWord() with that word
     */
    private fun checkIfWordSelected() {
        val selectedIndexes = letterStates.withIndex().filter { it.value == 1 || it.value == 3 }. map { it.index }
        val wordFound = wordBankSearch.filterValues { Arrays.equals(it.toIntArray(), selectedIndexes.toIntArray()) }
        if (wordFound.isNotEmpty()) {
            foundWord(wordBank.indexOf(wordFound.keys.first()))
        }
    }

    /**
     * The onClick of the Play Again button, which properly animates the view and resets the game
     */
    private fun playAgain() {
        setupGame(false)

        // Animates the change of color of the board and shows the right content
        animateColorBackground(500, gameBoard, R.color.white)
        animateHide(gameBoardFinished)
        animateShow(gameBoardContent)

        // Animates the height change of the game information top section and the game board
        val height = gameInfoHeight ?: 0
        val anim = ValueAnimator.ofInt(0, height)
        val topPadding = dpToPx(20)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = gameInfo.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.height = `val`
            layoutParams.setMargins(
                0,
                ((`val`.toFloat() / height) * topPadding.toFloat()).toInt(),
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
                layoutParams.height = height
                gameInfo.layoutParams = layoutParams
            }
        })
        anim.duration = 500
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

    }

    /**
     * The callback when the game is finished, it stops the time and animates the view to dismiss the game information
     */
    private fun finishedGame() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        // Stops the chronometer, get the elapsed seconds and displays it
        chronometer.stop()

        elapsedSeconds = ((SystemClock.elapsedRealtime() - chronometer.base) / 1000).toInt()

        val finishDescription: TextView = findViewById(R.id.finishDescription)
        finishDescription.text = String.format(resources.getString(R.string.finished_message), elapsedSeconds)

        // Updates the best time if the time is better than the previous one and unlocks the
        // achievement if the user has beat his best time
        var bestTime = getSharedPrefString(bestTimeValue) ?: ""
        if (bestTime == "" || bestTime.toInt() > elapsedSeconds) {
            if (bestTime != "" && account != null) {
                Games.getAchievementsClient(this, account)
                    .unlock(getString(R.string.achievement_youre_getting_better))
            }

            bestTime = elapsedSeconds.toString()
        }
        storeSharedPref(bestTimeValue, bestTime)

        // Animates the change of color of the board and shows the right content
        animateColorBackground(500, gameBoard, R.color.colorAccent)
        animateHide(gameBoardContent)
        animateShow(gameBoardFinished)

        // Animates the height change of the game information top section and the game board
        val height = gameInfoHeight ?: 0
        val anim = ValueAnimator.ofInt(height, 0)
        val topPadding = (gameInfo.layoutParams as ConstraintLayout.LayoutParams).topMargin
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = gameInfo.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.height = `val`
            layoutParams.setMargins(
                0,
                ((`val`.toFloat() / height) * topPadding.toFloat()).toInt(),
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

        // Unlocks the achievements
        if (account != null) {
            val gameClient = Games.getAchievementsClient(this, account)
            gameClient.increment(getString(R.string.achievement_played_10_times), 1)

            if (elapsedSeconds < 10) {
                gameClient.unlock(getString(R.string.achievement_you_are_fast))
            }
        }
    }

    /**
     * Called when a word is found
     *
     * @param wordIndex The index of the found word in the array for the word bank
     */
    private fun foundWord(wordIndex: Int) {
        // Checks if the word has not already been found and updates the score and the word bank
        if (!wordBankFound[wordIndex]) {
            score.text = (score.text.toString().toInt() + 1).toString()

            val child = wordBankLayout.getChildAt(wordIndex) as TextView
            child.paintFlags = child.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            wordBankFound[wordIndex] = true
        }

        // Checks if there are any other words left
        if (!wordBankFound.contains(false)) {
            finishedGame()
        }

        // Updates the state of the letters and updates the view's look accordingly
        wordBankSearch[wordBank[wordIndex]]?.forEach {
            letterStates[it] = 2
            val letter = letters.getChildAt(it) as TextView
            letter.setBackgroundResource(R.drawable.letter_found)
            letter.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    /**
     * Animates the color of the background of a CardView
     *
     * @param time The length of the animation in milliseconds
     * @param view The CardView to be animated
     * @param colorResourceTo The resource color to transitioned to
     */
    private fun animateColorBackground(time: Long, view: CardView, colorResourceTo: Int){
        val colorFrom = view.cardBackgroundColor.defaultColor
        val colorTo = ContextCompat.getColor(this, colorResourceTo)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = time
        colorAnimation.addUpdateListener { animator -> view.setCardBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == signInRequestCode) {
            handleSignInRequest(data, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Saves the information about the user in the shared preferences
                    storeSharedPref(profileIconURI, task.result?.iconImageUri.toString())
                    storeSharedPref(profileName, task.result?.name.toString())

                    updateProfileView(profileIcon, firstName, lastName)
                }
            })
        }
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val WORDS =
            arrayListOf("Swift", "ObjectiveC", "Java", "Kotlin", "Variable", "Mobile")
        private val OPTIONAL_WORDS = arrayListOf(
            "Android",
            "iOS",
            "Game",
            "Google",
            "Apple",
            "Phone",
            "Plants",
            "Tea"
        )
    }
}