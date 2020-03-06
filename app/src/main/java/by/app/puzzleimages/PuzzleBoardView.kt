package by.app.puzzleimages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import by.app.puzzleimages.PuzzleActivity.Companion.imageBitmap
import by.app.puzzleimages.PuzzleActivity.Companion.soundClick
import by.app.puzzleimages.PuzzleBoard.Companion.score
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*

class PuzzleBoardView(context: Context?) : View(context) {
    private val activity: Activity? = context as Activity?
    private var puzzleBoard: PuzzleBoard? = null
    private var animation: ArrayList<PuzzleBoard>?
    private val random = Random()
    private var countSolve = 0
    private var qapSecMax = 3
    private var gapSec = 0
    var imageWidth = 0
    fun initialize(imageBitmap: Bitmap?) {
        val width = width
        imageWidth = width + (width / 21)
        puzzleBoard = if (PuzzleBoard.NUM_TILES == 6) {
            PuzzleBoard(imageBitmap, width + (width / 180))
        } else {
            PuzzleBoard(imageBitmap, width)
        }
        refreshScreen()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (puzzleBoard != null) {
            if (animation != null && animation!!.size > 0) {
                countSolve++
                score = 0
                puzzleBoard = animation!!.removeAt(0)
                puzzleBoard!!.draw(canvas)
                if (animation!!.size == 0) {
                    animation = null
                    puzzleBoard!!.reset()
                    Toast.makeText(
                        activity,
                        String.format(
                            resources.getString(R.string.solution_steps),
                            countSolve - 1
                        ), Toast.LENGTH_LONG
                    ).show()
                    countSolve = 0
                } else {
                    this.postInvalidateDelayed(500)
                }
            } else {
                puzzleBoard!!.draw(canvas)
            }
        }
    }

    fun shuffle() {
        if (animation == null && puzzleBoard != null) {
            for (i in 0 until NUM_SHUFFLE_STEPS) {
                val boards =
                    puzzleBoard!!.neighbours()
                val randomIndex = random.nextInt(boards.size)
                puzzleBoard = boards[randomIndex]
                score = 0
            }
            refreshScreen()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (animation == null && puzzleBoard != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> if (puzzleBoard!!.click(event.x, event.y)) {
                    soundClick(context)
                    invalidate()
                    if (puzzleBoard!!.resolved()) {
                        val toast =
                            Toast.makeText(activity, "Congratulations!", Toast.LENGTH_SHORT)
                        toast.show()
                        endGame(context)
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // Showing when game is solved
    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    private fun endGame(context: Context) {
        val gameSolved = AlertDialog.Builder(context)
        with(gameSolved) {
            gameSolved.setCancelable(false)
            setTitle(R.string.game_end_title)
            if (gapSec >= qapSecMax) {
                setMessage(R.string.game_solved_failed)
            } else if (puzzleBoard!!.resolved()) {
                val text: String =
                    String.format(resources.getString(R.string.game_solved_success), score)
                setMessage(text)
                // Add result to DataBase
                    val dbHandler = DBHelper(context, null)
                    val user = score.toString()
                    dbHandler.addResult(user)
            }
            setNegativeButton(R.string.no) { _, _ ->
                score = 0
                val intent = Intent(context, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            setPositiveButton(R.string.yes) { _, _ ->
                readHighResult()
                initialize(imageBitmap)
                score = 0
            }
            show()
        }
    }

    // Read High Result from DataBase (PuzzleActivity)
    fun readHighResult() {
        val dbHandler = DBHelper(context, null)
        val cursor = dbHandler.getAllName()
        cursor!!.moveToFirst()
        if (!cursor.isAfterLast && cursor.getInt(0) > 0)
            (context as Activity).high_score.text = cursor.getString(0)
        cursor.close()
    }

    fun solve() {
        val boards =
            PriorityQueue(
                1,
                COMPARATOR
            )
        boards.add(puzzleBoard)

        var gapSecBegin = Calendar.getInstance().timeInMillis / 1000
        while (boards.size != 0) {
            val gapSecThis = Calendar.getInstance().timeInMillis / 1000
            if (gapSecThis - gapSecBegin >= 1) {
                gapSec += 1
                gapSecBegin = gapSecThis
            }
            if (gapSec >= qapSecMax) {
                Log.d("timeOut", "Time out, maximum time: $qapSecMax")
                Toast.makeText(context, "Wooops... Sorry...", Toast.LENGTH_SHORT).show()
                endGame(context)
                qapSecMax = 3
                gapSec = 0
                break
            }

            val retrievedBoard = boards.poll()
            if (!retrievedBoard!!.resolved()) {
                addNeighbours(boards, retrievedBoard)
            } else {
                boards.clear()
                val solvePath =
                    retrievedBoard.allPreviousBoards()
                solvePath.reverse()
                retrievedBoard.reset()
                animation = solvePath
                invalidate()
            }
        }
    }

    private fun addNeighbours(
        heap: PriorityQueue<PuzzleBoard?>,
        currentBoard: PuzzleBoard?
    ) {
        for (neighbour in currentBoard!!.neighbours()) {
            if (currentBoard.previousBoard == null ||
                !neighbour.sameStateAs(currentBoard.previousBoard)
            ) {
                neighbour.previousBoard = currentBoard
                heap.add(neighbour)
            }
        }
    }

    private fun refreshScreen() {
        puzzleBoard!!.reset()
        invalidate()
    }

    companion object {
        var NUM_SHUFFLE_STEPS = 40
        val COMPARATOR: Comparator<PuzzleBoard> =
            Comparator { puzzleBoard, t1 ->
                when {
                    puzzleBoard.priority() < t1.priority() -> {
                        -1
                    }
                    puzzleBoard.priority() > t1.priority() -> {
                        1
                    }
                    else -> {
                        0
                    }
                }
            }
    }

    init {
        animation = null
    }
}