package com.example.tic_tac_two.Domain
import kotlinx.serialization.Serializable
import java.sql.Timestamp

import java.util.Date
import java.util.HashMap

@Serializable
data class GameState (
    val id: Int = 0,
    val dt: Long = System.currentTimeMillis(),
    var currentRound: Int = 0,
    val totalRoundsBeforeGrid: Int = 6,
    val gridSize: Int = 5,
    var activeGridX: Int = 1,
    var activeGridY: Int = 1,
    var knotsCount: Int = 0,
    var crossesCount: Int = 0,
    var isActiveGridPhase: Boolean = false,
    var currentPlayer: String = "X",
    var gameover: Boolean = false,
    var gridState : HashMap<Int, Int> = HashMap()
)