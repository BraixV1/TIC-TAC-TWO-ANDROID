package com.example.tic_tac_two

import android.content.ClipData
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tic_tac_two.Domain.GameRepository
import com.example.tic_tac_two.Domain.GameState
import com.example.tic_tac_two.Domain.SavedGame
import ee.taltech.dbdemo.dal.DbHelper
import java.util.Dictionary
import java.util.HashMap
import kotlin.math.log

class GameActivity : AppCompatActivity() {



    private lateinit var MoonHighLight: ImageView
    private lateinit var SunHighLight: ImageView
    private var gameState: GameState = GameState()
    private lateinit var savedGame: SavedGame
    private lateinit var gameRepository: GameRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        gameRepository = GameRepository(this).open()
        MoonHighLight = findViewById(R.id.ImageView_Moon)
        SunHighLight = findViewById(R.id.ImageView_Sun)

        val savedGameId = intent.extras?.getInt("GAME_ID") ?: 0
        if (savedGameId > 0) {
            savedGame = gameRepository.getSavedGame(savedGameId)
            gameState = gameRepository.load(savedGame)
        } else {
            savedGame = SavedGame(name = "New Game", state = "")
            gameRepository.add(savedGame, gameState)
        }
        Log.d("Current player", gameState.currentPlayer)
        updateUIFromGameState()
        enableDragAndDrop()
    }




    private fun updateUIFromGameState() {
        // Highlight the correct player
        if (gameState.currentPlayer == "O") {
            MoonHighLight.setBackgroundResource(R.color.lightBLue)
            SunHighLight.setBackgroundResource(R.color.darkBlue)
        } else {
            MoonHighLight.setBackgroundResource(R.color.darkBlue)
            SunHighLight.setBackgroundResource(R.color.lightBLue)
        }

        // Update grid pieces based on gameState.gridState
        val gridLayout = findViewById<GridLayout>(R.id.gridWrapper)
        for (i in 0 until gridLayout.childCount) {
            val cell = gridLayout.getChildAt(i) as ImageView
            when (gameState.gridState[cell.id]) {
                1 -> {
                    cell.setImageResource(R.drawable.moon)
                    cell.tag = "X"
                }
                0 -> {
                    cell.setImageResource(R.drawable.sun)
                    cell.tag = "O"
                }
                else -> cell.setImageDrawable(null)
            }
        }
        highlightActiveGrid()
        winGameCheck()
    }


    fun placePiece(view: View) {
        val imageView = view as ImageView

        if(gameState.gameover) return



        // Check if the cell is empty and place a piece accordingly
        if (imageView.drawable == null) {
            if (gameState.currentPlayer == "X") {
                if(gameState.crossesCount < 4) {
                    imageView.setImageResource(R.drawable.moon)
                    gameState.gridState.put(imageView.id, 1)
                    imageView.tag = "X"
                    gameState.crossesCount++


                    gameState.currentRound++


                    if (winGameCheck()) {
                        switchCurrentPlayer()
                        gameRepository.update(savedGame, gameState)
                        enableDragAndDrop()
                    }


                }
            } else {
                if (gameState.knotsCount < 4) {
                    imageView.setImageResource(R.drawable.sun)
                    gameState.gridState.put(imageView.id, 0)
                    imageView.tag = "O"
                    gameState.knotsCount++


                    gameState.currentRound++


                    if (winGameCheck()) {
                        switchCurrentPlayer()
                        gameRepository.update(savedGame, gameState)
                        enableDragAndDrop()
                    }
                }
            }

        }

    }

    private fun winGameCheck(): Boolean {
        if(checkWin("X")){
            MoonHighLight.setBackgroundResource(R.color.yellow)
            gameState.gameover = true
            gameRepository.update(savedGame, gameState)
            return false
        }
        if(checkWin("O")) {
            SunHighLight.setBackgroundResource(R.color.yellow)
            gameState.gameover = true
            gameRepository.update(savedGame, gameState)
            return false
        }
        return true
    }



    private fun switchCurrentPlayer() {
        if (gameState.currentPlayer == "O") {
            gameState.currentPlayer = "X"
            MoonHighLight.setBackgroundResource(R.color.darkBlue)
            SunHighLight.setBackgroundResource(R.color.lightBLue)

        } else {
            gameState.currentPlayer = "O"
            MoonHighLight.setBackgroundResource(R.color.lightBLue)
            SunHighLight.setBackgroundResource(R.color.darkBlue)
        }
    }



    fun enableDragAndDrop() {
        val gridLayout = findViewById<GridLayout>(R.id.gridWrapper)
        for (i in 0 until gridLayout.childCount) {
            val cell = gridLayout.getChildAt(i) as ImageView
            cell.setOnLongClickListener {
                if (cell.drawable != null && cell.tag.toString() == gameState.currentPlayer) {
                    val clipData = ClipData.newPlainText("", "")
                    val dragShadowBuilder = View.DragShadowBuilder(cell)
                    cell.startDragAndDrop(clipData, dragShadowBuilder, cell, 0)
                }
                true
            }

            cell.setOnDragListener { view, dragEvent ->
                val dropTarget = view as ImageView
                when (dragEvent.action) {
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        dropTarget.setBackgroundResource(R.drawable.highlighted_cell)
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        dropTarget.setBackgroundResource(R.drawable.empty_cell)
                        highlightActiveGrid()
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        val draggedView = dragEvent.localState as ImageView
                        if (dropTarget.drawable == null && draggedView.tag == gameState.currentPlayer && dropTarget != draggedView) {
                            dropTarget.setImageDrawable(draggedView.drawable)
                            dropTarget.tag = draggedView.tag

                            // Update gameState with new positions
                            gameState.gridState[dropTarget.id] = gameState.gridState[draggedView.id] ?: -1
                            gameState.gridState.remove(draggedView.id)

                            draggedView.setImageDrawable(null)
                            draggedView.tag = null

                            if (winGameCheck()) {
                                switchCurrentPlayer()
                                gameRepository.update(savedGame, gameState)
                            }
                        }
                        true
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        dropTarget.setBackgroundResource(R.drawable.empty_cell)
                        highlightActiveGrid()
                        true
                    }
                    else -> false
                }
            }
        }
    }



    fun checkWin(playerTag: String): Boolean {
        val gridLayout = findViewById<GridLayout>(R.id.gridWrapper)

        val activeGridCells = Array(3) { Array(3) { "" } }

        for (i in 0 until gridLayout.childCount) {
            val row = i / gameState.gridSize
            val col = i % gameState.gridSize

            if (row in gameState.activeGridX until (gameState.activeGridX + 3) && col in gameState.activeGridY until (gameState.activeGridY + 3)) {
                val cell = gridLayout.getChildAt(i) as ImageView
                val localRow = row - gameState.activeGridX
                val localCol = col - gameState.activeGridY

                activeGridCells[localRow][localCol] = cell.tag?.toString() ?: ""
            }
        }

        // Check rows for a win
        for (row in 0..2) {
            if (activeGridCells[row][0].isNotEmpty() &&
                activeGridCells[row][0] == activeGridCells[row][1] &&
                activeGridCells[row][1] == activeGridCells[row][2] &&
                activeGridCells[row][0] == playerTag) {
                return true // Win detected in this row
            }
        }

        // Check columns for a win
        for (col in 0..2) {
            if (activeGridCells[0][col].isNotEmpty() &&
                activeGridCells[0][col] == activeGridCells[1][col] &&
                activeGridCells[1][col] == activeGridCells[2][col] &&
                activeGridCells[0][col] == playerTag) {
                return true // Win detected in this column
            }
        }

        // Check diagonals for a win
        if (activeGridCells[0][0].isNotEmpty() &&
            activeGridCells[0][0] == activeGridCells[1][1] &&
            activeGridCells[1][1] == activeGridCells[2][2] &&
            activeGridCells[0][0] == playerTag) {
            return true // Win detected in the main diagonal
        }

        if (activeGridCells[0][2].isNotEmpty() &&
            activeGridCells[0][2] == activeGridCells[1][1] &&
            activeGridCells[1][1] == activeGridCells[2][0] &&
            activeGridCells[0][2] == playerTag) {
            return true // Win detected in the anti-diagonal
        }

        return false // No win detected
    }


    private fun moveActiveGrid(direction: String) {

        var validDirection = false

        if (gameState.gameover) {
            return
        }
        when (direction) {
            "up" -> if (gameState.activeGridX > 0) {
                gameState.activeGridX--
                highlightActiveGrid()
                validDirection = true
            }
            "down" -> if (gameState.activeGridX < gameState.gridSize - 3) {
                gameState.activeGridX++
                highlightActiveGrid()
                validDirection = true
            }
            "left" -> if (gameState.activeGridY > 0) {
                gameState.activeGridY--
                highlightActiveGrid()
                validDirection = true
            }
            "right" -> if (gameState.activeGridY < gameState.gridSize - 3) {
                gameState.activeGridY++
                highlightActiveGrid()
                validDirection = true
            }
            "topRight" -> if((gameState.activeGridY < gameState.gridSize - 3) && (gameState.activeGridX > 0)) {
                gameState.activeGridX--
                gameState.activeGridY++
                highlightActiveGrid()
                validDirection = true
            }
            "topLeft" -> if ((gameState.activeGridY > 0) && (gameState.activeGridX > 0)) {
                gameState.activeGridX--
                gameState.activeGridY--
                highlightActiveGrid()
                validDirection = true
            }
            "bottomLeft" -> if((gameState.activeGridX < gameState.gridSize - 3) && (gameState.activeGridY > 0)) {
                gameState.activeGridX++
                gameState.activeGridY--
                highlightActiveGrid()
                validDirection = true
            }
            "bottomRight" -> if((gameState.activeGridX < gameState.gridSize - 3) && (gameState.activeGridY < gameState.gridSize - 3)) {
                gameState.activeGridX++
                gameState.activeGridY++
                highlightActiveGrid()
                validDirection = true
            }
        }

        if (validDirection) {
            if (winGameCheck()) {
                switchCurrentPlayer()
                gameRepository.update(savedGame, gameState)
                enableDragAndDrop()
            }
        }



    }

    fun moveGridUp(View: View) {
        moveActiveGrid("up")
    }

    fun moveGridDown(View: View) {
        moveActiveGrid("down")
    }

    fun moveGridLeft(View: View) {
        moveActiveGrid("left")
    }

    fun moveGridRight(View: View) {
        moveActiveGrid("right")
    }

    fun moveGridTopRight(View: View) {
        moveActiveGrid("topRight")
    }

    fun moveGridTopLeft(View: View) {
        moveActiveGrid("topLeft")
    }

    fun moveGridBottomLeft(View: View) {
        moveActiveGrid("bottomLeft")
    }

    fun moveGridBottomRight(View: View) {
        moveActiveGrid("bottomRight")
    }

    fun highlightActiveGrid() {
        val gridLayout = findViewById<GridLayout>(R.id.gridWrapper)
        for (i in 0 until gridLayout.childCount) {
            val cell = gridLayout.getChildAt(i)
            val row = i / gameState.gridSize
            val col = i % gameState.gridSize

            // Highlight cells within the active 3x3 grid
            if (row in gameState.activeGridX until (gameState.activeGridX + 3) && col in gameState.activeGridY until (gameState.activeGridY + 3)) {
                cell.setBackgroundResource(R.drawable.active_grid_background)
            } else {
                cell.setBackgroundResource(R.drawable.empty_cell)
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("gameId", savedGame.id)

        gameRepository.update(savedGame, gameState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val savedGameId = savedInstanceState.getInt("gameId")
        if (savedGameId > 0) {
            savedGame = gameRepository.getSavedGame(savedGameId)
            gameState = gameRepository.load(savedGame)
        }

        updateUIFromGameState()
        highlightActiveGrid()
        enableDragAndDrop()
        winGameCheck()
    }


    override fun onDestroy() {
        super.onDestroy()
        gameRepository.close()
    }










}