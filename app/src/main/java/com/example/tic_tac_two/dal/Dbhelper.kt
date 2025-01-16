package ee.taltech.dbdemo.dal

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "app.db"
        const val DATABASE_VERSION = 1

        const val GAME_TABLE_NAME = "GAMES"

        const val GAME_ID = "_id"
        const val GAME_NAME = "name"
        const val GAME_DT = "dt"
        const val GAME_STATE = "state"


        const val SQL_CREATE_TABLE =
            "create table $GAME_TABLE_NAME(" +
                    "$GAME_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$GAME_NAME TEXT NOT NULL, " +
                    "$GAME_DT INTEGER, " +
                    "$GAME_STATE TEXT NOT NULL);"

        const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " +
                "$GAME_TABLE_NAME";
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_TABLE)
        onCreate(db)
    }
}
