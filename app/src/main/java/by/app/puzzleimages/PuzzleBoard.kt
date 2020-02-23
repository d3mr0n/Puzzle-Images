package by.app.puzzleimages

import android.graphics.Bitmap
import android.graphics.Canvas
import java.util.*
import kotlin.math.abs

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING")
open class PuzzleBoard {
    private var tiles: ArrayList<PuzzleTile?>? =
        ArrayList()
    private var steps = 0
    var previousBoard: PuzzleBoard? = null

    internal constructor(bitmap: Bitmap?, parentWidth: Int) {
        var bitmap = bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap!!, parentWidth, parentWidth, false)
        val tileWidthAndHeight =
            bitmap.width / NUM_TILES
        for (i in 0 until NUM_TILES) {
            for (j in 0 until NUM_TILES) {
                val yStart = i * tileWidthAndHeight
                val xStart = j * tileWidthAndHeight
                val b = Bitmap.createBitmap(
                    bitmap,
                    xStart,
                    yStart,
                    tileWidthAndHeight,
                    tileWidthAndHeight
                )
                if (i == NUM_TILES - 1 && j == NUM_TILES - 1) {
                    tiles!!.add(null)
                } else {
                    tiles!!.add(
                        PuzzleTile(
                            b,
                            i * NUM_TILES + j
                        )
                    )
                }
            }
        }
    }

    internal constructor(otherBoard: PuzzleBoard) {
        steps = otherBoard.steps + 1
        tiles = otherBoard.tiles!!.clone() as ArrayList<PuzzleTile?>
    }

    fun reset() {
        previousBoard = null
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) false else tiles == (other as PuzzleBoard).tiles
    }

    fun draw(canvas: Canvas) {
        if (tiles == null) {
            return
        }
        for (i in 0 until NUM_TILES * NUM_TILES) {
            val tile = tiles!![i]
            tile?.draw(
                canvas,
                i % NUM_TILES,
                i / NUM_TILES
            )
        }
    }

    fun click(x: Float, y: Float): Boolean {
        for (i in 0 until NUM_TILES * NUM_TILES) {
            val tile = tiles!![i]
            if (tile != null) {
                if (tile.isClicked(
                        x,
                        y,
                        i % NUM_TILES,
                        i / NUM_TILES
                    )
                ) {
                    return tryMoving(
                        i % NUM_TILES,
                        i / NUM_TILES
                    )
                }
            }
        }
        return false
    }

    private fun tryMoving(tileX: Int, tileY: Int): Boolean {
        for (delta in NEIGHBOUR_COORDS) {
            val nullX = tileX + delta[0]
            val nullY = tileY + delta[1]
            if (nullX in 0 until NUM_TILES && nullY >= 0 && nullY < NUM_TILES && tiles!![xyToIndex(
                    nullX,
                    nullY
                )] == null
            ) {
                score++
                swapTiles(xyToIndex(nullX, nullY), xyToIndex(tileX, tileY))
                return true
            }
        }
        return false
    }

    fun resolved(): Boolean {
        for (i in 0 until NUM_TILES * NUM_TILES - 1) {
            val tile = tiles!![i]
            if (tile == null || tile.number != i) return false
        }
        return true
    }

    private fun xyToIndex(x: Int, y: Int): Int {
        return x + y * NUM_TILES
    }

    private fun swapTiles(i: Int, j: Int) {
        val temp = tiles!![i]
        tiles!![i] = tiles!![j]
        tiles!![j] = temp
    }

    fun neighbours(): ArrayList<PuzzleBoard> {
        val neighbours =
            ArrayList<PuzzleBoard>()
        val indexOfEmptyTile: Int = tiles!!.indexOf(null)
        val emptyX = indexOfEmptyTile % NUM_TILES
        val emptyY = indexOfEmptyTile / NUM_TILES
        var tileX: Int
        var tileY: Int
        for (delta in NEIGHBOUR_COORDS) {
            tileX = emptyX + delta[0]
            tileY = emptyY + delta[1]
            // If move is within bounds:
            if (tileX in 0 until NUM_TILES && tileY >= 0 && tileY < NUM_TILES) {
                val p = PuzzleBoard(this)
                p.tryMoving(tileX, tileY)
                neighbours.add(p)
            }
        }
        return neighbours
    }

    fun priority(): Int {
        var tileX: Int
        var tileY: Int
        var desiredX: Int
        var desiredY: Int
        var manhattanDistance = 0
        for (i in tiles!!.indices) {
            if (tiles!![i] != null) {
                tileX = i % NUM_TILES
                tileY = i / NUM_TILES
                desiredX =
                    tiles!![i]!!.number % NUM_TILES
                desiredY =
                    tiles!![i]!!.number / NUM_TILES
                manhattanDistance += abs(tileX - desiredX) + abs(tileY - desiredY)
            }
        }
        return manhattanDistance + steps
    }

    fun allPreviousBoards(): ArrayList<PuzzleBoard> {
        val previousBoards =
            ArrayList<PuzzleBoard>()
        var b = previousBoard
        previousBoards.add(this)
        while (b != null) {
            previousBoards.add(b)
            b = b.previousBoard
        }
        return previousBoards
    }

    fun sameStateAs(otherBoard: PuzzleBoard?): Boolean {
        for (i in tiles!!.indices) {
            if (tiles!![i] != null && otherBoard!!.tiles!![i] != null) {
                if (tiles!![i]!!.number != otherBoard.tiles!![i]!!.number) {
                    return false
                }
            }
        }
        return true
    }

    companion object {
        var score = 0
        var NUM_TILES = 3
        private val NEIGHBOUR_COORDS = arrayOf(
            intArrayOf(-1, 0),
            intArrayOf(1, 0),
            intArrayOf(0, -1),
            intArrayOf(0, 1)
        )
    }
}