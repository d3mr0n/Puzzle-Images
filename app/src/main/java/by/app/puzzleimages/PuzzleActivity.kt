package by.app.puzzleimages

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import androidx.preference.PreferenceManager
import by.app.puzzleimages.PuzzleBoard.Companion.score
import by.app.puzzleimages.PuzzleBoardView.Companion.NUM_SHUFFLE_STEPS
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.dialog_original_picture.view.*
import java.io.File
import java.io.IOException

@Suppress(
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION", "NAME_SHADOWING",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNUSED_PARAMETER"
)
class PuzzleActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private var boardView: PuzzleBoardView? = null
    private var photoURI: Uri? = null
    private var photo: File? = null
    private var shuffleSteps = 40

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // This fix camera launch for some api levels
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
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
        boardView!!.readHighResult()
        countScore.start()
    }

    // This will add local image of app to puzzle Relativelayout
    // as well as the background and frame of puzzles
    private fun putImagePuzzle() {
        Handler().postDelayed({
            imageCheckNumTiles()
            boardView!!.initialize(imageBitmap)

            img.layoutParams.height = boardView!!.imageWidth
            Log.d("Width", "" + boardView!!.imageWidth)
            img.requestLayout()
        }, 10)
    }

    private fun imageCheckNumTiles() {
        when (PuzzleBoard.NUM_TILES) {
            3 -> imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.tiles_three)
            4 -> imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.tiles_four)
            5 -> imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.tiles_five)
            6 -> imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.tiles_six)
        }
    }

    // Counting Score of tile moves
    private var countScore: Thread = object : Thread() {
        @SuppressLint("SetTextI18n")
        override fun run() {
            try {
                while (!this.isInterrupted) {
                    sleep(500)
                    runOnUiThread { score_count.text = "" + score }
                }
            } catch (e: InterruptedException) {
            }
        }
    }

    // Double click 'Back' buttn to return from game
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            score = 0
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            R.string.hold_back,
            Toast.LENGTH_SHORT
        ).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_puzzle, menu)
        return true
    }

    // For change Shuffle Steps after return from settings
    override fun onResume() {
        val pref: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(this)
        val listValue = pref.getString("shuffle_steps_key", "40")!!.toInt()
        this.shuffleSteps = listValue
        super.onResume()
    }

    // Buttons of ActionBar
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.action_shuffle -> {
            NUM_SHUFFLE_STEPS = shuffleSteps
            Log.d(TAG, "Size Shuffle: $NUM_SHUFFLE_STEPS")
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
        R.id.action_visibility -> {
            showImageOriginal()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Dialog with the original puzzle image
    @SuppressLint("InflateParams")
    fun showImageOriginal() {
        val alertDialog: AlertDialog?
        val dialogBuilder = AlertDialog.Builder(this)
        val layoutView: View = layoutInflater.inflate(R.layout.dialog_original_picture, null)
        layoutView.imageHint.setImageBitmap(imageBitmap)
        layoutView.imageHint.layoutParams.height = boardView!!.imageWidth
        layoutView.imageHint.layoutParams.width = boardView!!.imageWidth
        layoutView.imageHint.requestLayout()
        dialogBuilder.setView(layoutView)
        alertDialog = dialogBuilder.create()
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        layoutView.dialog_hint.setOnClickListener { alertDialog.dismiss() }
    }

    fun highScoreView(view: View) {
        MainActivity.highScoreShow(this)
    }

    // Call activity for camera or gallery
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

    // Take Picture from Camera
    private fun dispatchTakeCamera() {
        val takePicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicIntent.resolveActivity(packageManager) != null) {
            try {
                photo = createImageFile()
            } catch (ex: IOException) {
                Log.v(TAG, "Couldn't create File photo :", ex)
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
        val ei = ExifInterface(imageUri!!.path!!)
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
    // With checking checkbox in settings
    private fun deletePicHistory() {
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                "picture_history_switch",
                true
            )
        ) {
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
        // Sound when tiles are moves with SwitchPreference check
        fun soundClick(context: Context) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    "sound_switch",
                    true
                )
            ) {
                val mp: MediaPlayer = MediaPlayer.create(context, R.raw.sound_chip)
                mp.setOnCompletionListener { mp ->
                    mp.reset()
                    mp.release()
                    Log.e("Sound", "Click")
                }
                mp.start()
            }
        }

        private const val TAG = "PuzzleActivity"
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGE_REQUEST = 2
        var imageBitmap: Bitmap? = null
    }
}