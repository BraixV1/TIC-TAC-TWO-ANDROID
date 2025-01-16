package com.example.tic_tac_two

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
import com.example.tic_tac_two.Domain.GameRepository

class LoadGameActivity : AppCompatActivity() {

    private lateinit var gameRepository: GameRepository
    private lateinit var savedGamesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loadgames)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameRepository = GameRepository(this).open()
        savedGamesContainer = findViewById(R.id.LinearLayout_Games)
        loadSavedGames()
    }

    private fun loadSavedGames() {
        val savedGames = gameRepository.getAll()

        for (savedGame in savedGames) {

            var state = gameRepository.load(savedGame)

            val gameLinearLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }

                setBackgroundColor(ContextCompat.getColor(context, R.color.pink))

                tag = savedGame.id
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    loadGame(savedGame.id)
                }
            }

            val gameImageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    200,
                    200
                ).apply {
                    setMargins(0, 0, 16, 0)
                }
                if(state.currentPlayer == "X") {
                    setImageResource(R.drawable.moon)
                }
                if (state.currentPlayer == "O") {
                    setImageResource(R.drawable.sun)
                }

                Log.d("Current player", state.currentPlayer)

                setBackgroundColor(ContextCompat.getColor(context, R.color.darkBlue))

                contentDescription = "Saved game ${savedGame.name}"
            }

            val timeText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "Game ID: ${savedGame.id} \nGame over: ${state.gameover}"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(8, 8, 8, 8)
            }

            gameLinearLayout.addView(gameImageView)
            gameLinearLayout.addView(timeText)
            savedGamesContainer.addView(gameLinearLayout)
        }
    }


    private fun loadGame(gameId: Int) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_ID", gameId)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        gameRepository.close()
    }

}