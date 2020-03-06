package by.app.puzzleimages

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) :
    SQLiteOpenHelper(
        context, DATABASE_NAME,
        factory, 1
    ) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_three INTEGER," +
                    "$COLUMN_four INTEGER, $COLUMN_five INTEGER, $COLUMN_six INTEGER);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    // Adding result of current game
    fun addResult(name: String?) {
        val values = ContentValues()
        when (PuzzleBoard.NUM_TILES) {
            3 -> values.put(COLUMN_three, name)
            4 -> values.put(COLUMN_four, name)
            5 -> values.put(COLUMN_five, name)
            6 -> values.put(COLUMN_six, name)
        }
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // Get All Max records for ResultsTable
    fun getAllMaxRecords(level: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT MAX($level) FROM $TABLE_NAME", null)
        cursor.moveToFirst()
        val result = cursor.getString(0)
        cursor.close()
        return result
    }

    // Get Max record for current game
    @SuppressLint("Recycle")
    fun getAllName(): Cursor? {
        val db = this.readableDatabase
        return when (PuzzleBoard.NUM_TILES) {
            3 ->
                db.rawQuery("SELECT MAX($COLUMN_three) FROM $TABLE_NAME", null)
            4 ->
                db.rawQuery("SELECT MAX($COLUMN_four) FROM $TABLE_NAME", null)
            5 ->
                db.rawQuery("SELECT MAX($COLUMN_five) FROM $TABLE_NAME", null)
            6 ->
                db.rawQuery("SELECT MAX($COLUMN_six) FROM $TABLE_NAME", null)

            else -> db.rawQuery("SELECT MAX(*) FROM $TABLE_NAME", null)
        }
    }

    // Delete All result records and set result to '0' everywhere
    fun deleteAllRecords() {
        val db = this.writableDatabase
        db.execSQL("DELETE from $TABLE_NAME")
        db.execSQL(
            "INSERT INTO $TABLE_NAME ($COLUMN_three, $COLUMN_four, " +
                    "$COLUMN_five, $COLUMN_six) VALUES(0, 0, 0, 0)"
        )
        db.close()
    }

    companion object {
        const val DATABASE_NAME = "results.db"
        const val TABLE_NAME = "results_table"
        const val COLUMN_three = "three"
        const val COLUMN_four = "four"
        const val COLUMN_five = "five"
        const val COLUMN_six = "six"
    }
}