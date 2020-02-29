package by.app.puzzleimages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import by.app.puzzleimages.PuzzleActivity.Companion.continueGame
import by.app.puzzleimages.PuzzleBoard.Companion.score
import java.util.*

class PuzzleBoardView(context: Context?) : View(context) {
    private val activity: Activity? = context as Activity?
    private var puzzleBoard: PuzzleBoard? = null
    private var animation: ArrayList<PuzzleBoard>?
    private val random = Random()
    private var countSolve = 0
    var imageWidth = 0
    fun initialize(imageBitmap: Bitmap?) {
        val width = width
        imageWidth = width + (width / 21)
        puzzleBoard = PuzzleBoard(imageBitmap, width)
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
                    invalidate()
                    if (puzzleBoard!!.resolved()) {
                        val toast =
                            Toast.makeText(activity, "Congratulations!", Toast.LENGTH_SHORT)
                        toast.show()
                        continueGame(context)
                    } else {
                        soundClick()
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun solve() {
        val boards =
            PriorityQueue(
                1,
                COMPARATOR
            )
        boards.add(puzzleBoard)
        while (boards.size != 0) {
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

    private fun soundClick() {
        val ring = MediaPlayer.create(context, R.raw.sound_chip)
        ring.start()
    }

    companion object {
        const val NUM_SHUFFLE_STEPS = 40
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