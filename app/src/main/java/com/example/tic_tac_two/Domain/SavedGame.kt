package com.example.tic_tac_two.Domain



data class SavedGame (
    var id: Int = 0,
    val dt: Long = 0,
    val name: String,
    val state: String
)