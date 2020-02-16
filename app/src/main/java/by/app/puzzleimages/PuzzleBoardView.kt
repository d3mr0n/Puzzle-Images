package by.app.puzzleimages

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.util.*

class PuzzleBoardView(context: Context?) : View(context) {
    private val activity: Activity?
    private var puzzleBoard: PuzzleBoard? = null
    private var animation: ArrayList<PuzzleBoard>?
    private val random = Random()
    fun initialize(imageBitmap: Bitmap?) {
        val width = width
        puzzleBoard = PuzzleBoard(imageBitmap, width)
        refreshScreen()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (puzzleBoard != null) {
            if (animation != null && animation!!.size > 0) {
                puzzleBoard = animation!!.removeAt(0)
                puzzleBoard!!.draw(canvas)
                if (animation!!.size == 0) {
                    animation = null
                    puzzleBoard!!.reset()
                    val toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG)
                    toast.show()
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
                val randomIndex = random.nextInt(boards!!.size)
                puzzleBoard = boards[randomIndex]
            }
            refreshScreen()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (animation == null && puzzleBoard != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> if (puzzleBoard!!.click(event.x, event.y)) {
                    invalidate()
                    if (puzzleBoard!!.resolved()) {
                        val toast =
                            Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG)
                        toast.show()
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
                Collections.reverse(solvePath)
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
                !neighbour!!.sameStateAs(currentBoard.previousBoard)
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
        const val NUM_SHUFFLE_STEPS = 40
        val COMPARATOR: Comparator<PuzzleBoard> =
            Comparator<PuzzleBoard> { puzzleBoard, t1 ->
                if (puzzleBoard.priority() < t1.priority()) {
                    -1
                } else if (puzzleBoard.priority() > t1.priority()) {
                    1
                } else {
                    0
                }
            }
    }

    init {
        activity = context as Activity?
        animation = null
    }
}