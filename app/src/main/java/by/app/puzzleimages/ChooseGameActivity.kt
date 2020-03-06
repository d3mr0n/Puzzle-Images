package by.app.puzzleimages

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_choose_game.*

class ChooseGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Show High results in Activity Choosing Game
    override fun onStart() {
        val dbHandler = DBHelper(this, null)
        record_one.setText(R.string.high_score_choose_game)
        record_two.setText(R.string.high_score_choose_game)
        record_three.setText(R.string.high_score_choose_game)
        record_four.setText(R.string.high_score_choose_game)
        try {
            record_one.append(dbHandler.getAllMaxRecords(DBHelper.COLUMN_three))
            record_two.append(dbHandler.getAllMaxRecords(DBHelper.COLUMN_four))
            record_three.append(dbHandler.getAllMaxRecords(DBHelper.COLUMN_five))
            record_four.append(dbHandler.getAllMaxRecords(DBHelper.COLUMN_six))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStart()
    }

    fun onClickCard(v: View) {
        when (v.id) {
            R.id.threeCardId -> PuzzleBoard.NUM_TILES = 3
            R.id.fourCardId -> PuzzleBoard.NUM_TILES = 4
            R.id.fiveCardId -> PuzzleBoard.NUM_TILES = 5
            R.id.sixCardId -> PuzzleBoard.NUM_TILES = 6
        }
        startActivity(Intent(this, PuzzleActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
