package by.app.puzzleimages

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import by.app.puzzleimages.PuzzleBoard.Companion.score
import kotlinx.android.synthetic.main.activity_game.*
import java.io.File
import java.io.IOException

class PuzzleActivity : AppCompatActivity() {
    private var imageBitmap: Bitmap? = null
    private var boardView: PuzzleBoardView? = null
    private var photoURI: Uri? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // This code programmatically adds the PuzzleBoardView to the UI.
        val container = findViewById(R.id.puzzle_container) as RelativeLayout
        boardView = PuzzleBoardView(this)
        // Some setup of the view.
        boardView!!.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(boardView)
        thread.start()
    }

    // Эto pisec costil'. But it work :)
    var thread: Thread = object : Thread() {
        override fun run() {
            try {
                while (!this.isInterrupted) {
                    sleep(500)
                    runOnUiThread {
                        score_count.text = "Score: " + score
                    }
                }
            } catch (e: InterruptedException) {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_puzzle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle action bar item clicks here. The action bar will
// automatically handle clicks on the Home/Up button, so long
// as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    fun dispatchTakePictureIntent(view: View?) {
        val takePicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            var photo: File? = null
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

    fun dispatchTakeGallery(view: View?) {
        val takePicIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(
                takePicIntent,
                PICK_IMAGE_REQUEST
            )
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI)
                imageBitmap = rotateImageIfRequired(imageBitmap, photoURI)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cropImageToSquare()
            boardView!!.initialize(imageBitmap)
            deletePicHistory()
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data
            val bitmap: Bitmap? = null
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            boardView!!.initialize(imageBitmap)
        }
    }

    fun shuffleImage(view: View?) {
        boardView!!.shuffle()
    }

    fun solve(view: View?) {
        boardView!!.solve()
    }

    fun useStockPic(view: View?) {
        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img)
        boardView!!.initialize(imageBitmap)
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
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(image, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(image, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(image, 270)
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
            val cropImg = Bitmap.createBitmap(imageBitmap!!, 0, crop, width, width)
        } else if (width > height) {
            val crop = (width - height) / 2
            val cropImg = Bitmap.createBitmap(imageBitmap!!, crop, 0, height, height)
        }
    }

    fun dec_N(view: View?) {
        PuzzleBoard.Companion.NUM_TILES++
    }

    fun inc_N(view: View?) {
        PuzzleBoard.Companion.NUM_TILES--
    }

    companion object {
        private const val TAG = "PuzzleActivity"
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGE_REQUEST = 2
    }
}