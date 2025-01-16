package com.example.tic_tac_two.Domain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ee.taltech.dbdemo.dal.DbHelper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameRepository(val context: Context) {

    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): GameRepository {
        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }


    fun add(game: SavedGame, state: GameState) {
        val contentValues = ContentValues()
        contentValues.put(DbHelper.GAME_NAME, game.name)
        contentValues.put(DbHelper.GAME_DT, System.currentTimeMillis())
        contentValues.put(DbHelper.GAME_STATE, Json.encodeToString(state))

        val newId = db.insert(DbHelper.GAME_TABLE_NAME, null, contentValues)
        if (newId.toInt() != -1) {
            game.id = newId.toInt()
        }
    }

    fun update(game: SavedGame, state: GameState) {
        val whereClause = "${DbHelper.GAME_ID} = ?"
        val whereArgs = arrayOf(game.id.toString())
        val contentValues = ContentValues()
        contentValues.put(DbHelper.GAME_NAME, game.name)
        contentValues.put(DbHelper.GAME_DT, System.currentTimeMillis())
        contentValues.put(DbHelper.GAME_STATE, Json.encodeToString(state))

        db.update(DbHelper.GAME_TABLE_NAME, contentValues, whereClause, whereArgs)
    }

    fun getAll(): List<SavedGame> {
        val cursor = db.query(
            DbHelper.GAME_TABLE_NAME,
            null,
            null, null, null, null, null
        )

        val res = ArrayList<SavedGame>()

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            res.add(
                SavedGame(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.GAME_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.GAME_NAME)),
                    dt = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.GAME_DT)),
                    state = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.GAME_STATE)),
                )
            )
            cursor.moveToNext()
        }
        cursor.close()
        return res
    }

    fun getSavedGame(id: Int): SavedGame {
        val cursor = db.query(
            DbHelper.GAME_TABLE_NAME,
            arrayOf(DbHelper.GAME_ID, DbHelper.GAME_NAME, DbHelper.GAME_DT, DbHelper.GAME_STATE),
            "${DbHelper.GAME_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            SavedGame(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.GAME_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.GAME_NAME)),
                dt = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.GAME_DT)),
                state = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.GAME_STATE)),
            )
        } else {
            SavedGame(id = -1, name = "Default", dt = 0, state = "")
        }.also {
            cursor.close()  // Ensure the cursor is closed to prevent memory leaks
        }
    }


    fun load(game : SavedGame): GameState {
        return Json.decodeFromString<GameState>(game.state)
    }
}