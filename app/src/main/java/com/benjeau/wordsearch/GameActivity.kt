package com.benjeau.wordsearch

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import android.util.TypedValue
import android.opengl.ETC1.getHeight
import android.view.ViewTreeObserver





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

        val dip = 5f
        val r = resources
        val px: Int = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        ).toInt()

        wordBank.forEach{
            val text = TextView(this)
            wordBankText.add(text)
            wordBankLayout.addView(text)
            text.text = it
            text.setTextColor(resources.getColor(R.color.white))
            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.setPadding(px, px, px, px)
        }

        wordBankLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                wordBankLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                wordBankText.forEach{it.width = wordBankLayout.width/4}
            }
        })
    }
}
