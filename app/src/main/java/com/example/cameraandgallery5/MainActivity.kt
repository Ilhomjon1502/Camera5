package com.example.cameraandgallery5

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.cameraandgallery5.databinding.ActivityMainBinding
import com.github.florent37.runtimepermission.RuntimePermission.askPermission
import com.github.florent37.runtimepermission.kotlin.askPermission
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var currentImagePath: String
    val REQUEST_CODE = 1
    lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraBtnOld.setOnClickListener {
            askPermission(Manifest.permission.CAMERA){
                //all permissions already granted or just granted

                var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent,0)

            }.onDeclined { e ->
                if (e.hasDenied()) {

                    AlertDialog.Builder(this)
                        .setMessage("Please accept our permissions")
                        .setPositiveButton("yes") { dialog, which ->
                            e.askAgain();
                        } //ask again
                        .setNegativeButton("no") { dialog, which ->
                            dialog.dismiss();
                        }
                        .show();
                }

                if(e.hasForeverDenied()) {
                    //the list of forever denied permissions, user has check 'never ask again'

                    // you need to open setting manually if you really need it
                    e.goToSettings();
                }
            }
        }

        binding.cameraBtnNew.setOnClickListener {
            val imageFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID,
                imageFile
            )
            println("mageFile: $imageFile, photoUri = $photoUri")
            getTakeImageContent.launch(photoUri)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private val getTakeImageContent =
        registerForActivityResult(ActivityResultContracts.TakePicture()){
            println("getTakeImageContent ishlayapti")
            if (it) {
                println(photoUri)
                val date=SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                binding.showImage.setImageURI(photoUri)
                val file = File(filesDir, "$date.jpg")
                val inputStream = contentResolver?.openInputStream(photoUri)
                val fileOutputStream = FileOutputStream(file)
                inputStream?.copyTo(fileOutputStream)
                inputStream?.close()
                fileOutputStream.close()
                val absolutePath = file.absolutePath
                Toast.makeText(this, "$absolutePath", Toast.LENGTH_SHORT).show()
            }
        }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        println("createImageFile ishlayapti")
        return File.createTempFile("JPEG_$format", ".jpg", externalFilesDir).apply {
            currentImagePath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == AppCompatActivity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            binding.showImage.setImageBitmap(bitmap)
        }
    }
}