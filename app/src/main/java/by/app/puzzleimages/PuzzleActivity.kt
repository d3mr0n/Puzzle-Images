package by.app.puzzleimages

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ExifInterface.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.app.puzzleimages.PuzzleBoard.Companion.score
import kotlinx.android.synthetic.main.activity_game.*
import java.io.File
import java.io.IOException

@Suppress(
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION", "NAME_SHADOWING",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class PuzzleActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private var imageBitmap: Bitmap? = null
    private var boardView: PuzzleBoardView? = null
    private var photoURI: Uri? = null
    private var photo: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // This code programmatically adds the PuzzleBoardView to the UI.
        val container = findViewById<RelativeLayout>(R.id.puzzle_container)
        boardView = PuzzleBoardView(this)
        // Some setup of the view.
        boardView!!.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(boardView)
        putImagePuzzle()
        countScore.start()
    }

    // This will add local image of app to puzzle Relativelayout
    // as well as the background and frame of puzzles
    private fun putImagePuzzle() {
        Handler().postDelayed({
            imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.img)
            cropImageToSquare()
            boardView!!.initialize(imageBitmap)

            img.getLayoutParams().height = boardView!!.image_width
            Log.d("Width", "" + boardView!!.image_width)
            img.requestLayout()
        }, 10)
    }

    // Counting Score of tile moves
    private var countScore: Thread = object : Thread() {
        @SuppressLint("SetTextI18n")
        override fun run() {
            try {
                while (!this.isInterrupted) {
                    sleep(500)
                    runOnUiThread {
                        score_count.text = "" + score
                        if (score_count.text.length >= high_score.text.length) {
                            high_score.text = score_count.text
                        }
                    }
                }
            } catch (e: InterruptedException) {
            }
        }
    }

    // Double click 'Back' buttn to return from game
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            "Please click BACK again to exit\nThe game will be reset!",
            Toast.LENGTH_SHORT
        ).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_puzzle, menu)
        return true
    }

    // Buttons of ActionBar
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.action_shuffle -> {
            boardView!!.shuffle()
            true
        }
        R.id.action_solve -> {
            boardView!!.solve()
            true
        }
        R.id.action_camera -> {
            dispatchTakeCamera()
            true
        }
        R.id.action_gallery -> {
            dispatchTakeGallery()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Show Dialog of all results
    @SuppressLint("InflateParams")
    fun highScoreView(view: View) {
        val inflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.dialog_result, null)
        val dialog = Dialog(this)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val dialogButton: Button = dialog.findViewById<View>(R.id.btn_close_result) as Button
        dialogButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Take Picture from Camera
    private fun dispatchTakeCamera() {
        val takePicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicIntent.resolveActivity(packageManager) != null) {
            try {
                photo = createImageFile()
            } catch (ex: IOException) {
                Log.v(
                    TAG,
                    "Couldn't create File photo :",
                    ex
                )
            }
            if (photo != null) {
                photoURI = Uri.fromFile(photo)
                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(
                    takePicIntent,
                    REQUEST_IMAGE_CAPTURE
                )
            }
        }
    }

    // Take Picture from Gallery
    private fun dispatchTakeGallery() {
        val takePicIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (takePicIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(
                takePicIntent,
                PICK_IMAGE_REQUEST
            )
        }
    }

    // Call
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoURI)
                imageBitmap = rotateImageIfRequired(imageBitmap, photoURI)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cropImageToSquare()
            boardView!!.initialize(imageBitmap)
            deletePicHistory()
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageBitmap = rotateImageIfRequired(imageBitmap, imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cropImageToSquare()
            boardView!!.initialize(imageBitmap)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File? =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("photo", ".jpg", storageDir)
    }

    // Some phones store image in landscape, some in portrait.
    // In either case, rotate them appropriately. Code taken from StackOverflow.
    @Throws(IOException::class)
    private fun rotateImageIfRequired(image: Bitmap?, imageUri: Uri?): Bitmap? {
        val ei = ExifInterface(imageUri!!.path)
        val orientation = ei.getAttributeInt(
            TAG_ORIENTATION,
            ORIENTATION_NORMAL
        )
        return when (orientation) {
            ORIENTATION_ROTATE_90 -> rotateImage(image, 90)
            ORIENTATION_ROTATE_180 -> rotateImage(image, 180)
            ORIENTATION_ROTATE_270 -> rotateImage(image, 270)
            else -> image
        }
    }

    private fun rotateImage(image: Bitmap?, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImage = Bitmap.createBitmap(
            image!!, 0, 0, image.width,
            image.height, matrix, true
        )
        image.recycle()
        return rotatedImage
    }

    // Make sure photos aren't being needlessly persistently kept in phone storage:
    private fun deletePicHistory() {
        val dir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return
        val path = dir.toString()
        Log.d("Files", "Path: $path")
        val f = File(path)
        val files = f.listFiles()
        Log.d("Files", "Size: " + files.size)
        for (file in files) {
            Log.d("Files", "FileName: " + file.name)
            file.delete()
        }
    }

    // Crop to a square before scaling the image in PuzzleBoardView
    private fun cropImageToSquare() {
        val width = imageBitmap!!.width
        val height = imageBitmap!!.height
        if (height > width) {
            val crop = (height - width) / 2
            imageBitmap = Bitmap.createBitmap(imageBitmap!!, 0, crop, width, width)
        } else if (width > height) {
            val crop = (width - height) / 2
            imageBitmap = Bitmap.createBitmap(imageBitmap!!, crop, 0, height, height)
        }
    }

    companion object {
        private const val TAG = "PuzzleActivity"
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGE_REQUEST = 2
    }
}