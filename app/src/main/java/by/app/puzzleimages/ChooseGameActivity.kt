package by.app.puzzleimages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View

class ChooseGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun onClick(v: View) {
        when (v.getId()) {
            R.id.area0 -> PuzzleBoard.NUM_TILES = 3
            R.id.area -> PuzzleBoard.NUM_TILES = 4
            R.id.weight -> PuzzleBoard.NUM_TILES = 5
            R.id.currency -> PuzzleBoard.NUM_TILES = 6
            R.id.temperature -> PuzzleBoard.NUM_TILES = 7
            R.id.power -> PuzzleBoard.NUM_TILES = 8
        }
        startActivity(Intent(this, PuzzleActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
