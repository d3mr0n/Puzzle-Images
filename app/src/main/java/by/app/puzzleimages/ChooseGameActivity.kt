package by.app.puzzleimages

import android.annotation.SuppressLint
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

    // Show High result in Activity Choosing Game
    @SuppressLint("SetTextI18n")
    override fun onStart() {
        val dbHandler = DBHelper(this, null)
        record_one.text = getString(R.string.high_score_choose_game) +
                dbHandler.getAllMaxRecords(DBHelper.COLUMN_three)
        record_two.text = getString(R.string.high_score_choose_game) +
                dbHandler.getAllMaxRecords(DBHelper.COLUMN_four)
        record_three.text = getString(R.string.high_score_choose_game) +
                dbHandler.getAllMaxRecords(DBHelper.COLUMN_five)
        record_four.text = getString(R.string.high_score_choose_game) +
                dbHandler.getAllMaxRecords(DBHelper.COLUMN_six)
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
