package com.example.android_tp_integrador

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout

class PhotoActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private val STORAGE_PERMISSION_CODE = 103

    private lateinit var imageContainer: ConstraintLayout
    private val imageList = mutableListOf<Bitmap>() // Lista de imágenes

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_photo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.photoActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageContainer = findViewById(R.id.imageContainer)

        var openCameraBtn: Button = findViewById(R.id.openCameraButton)
        openCameraBtn.setOnClickListener {
            checkCameraPermission()
        }

        val openGalleryBtn: Button = findViewById(R.id.openGaleryButton)
        openGalleryBtn.setOnClickListener {
            checkStoragePermission()
        }
    }

    // Método para verificar y solicitar permisos
    fun checkCameraPermission() {
        // Me fijo si ya tiene el permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Solicito permiso
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    addImageToLayout(imageBitmap)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        val bitmap = uriToBitmap(selectedImageUri)
                        if (bitmap != null) {
                            addImageToLayout(bitmap)
                        }
                    }
                }
            }
        }
    }

    // Función para agregar la imagen al layout
    private fun addImageToLayout(bitmap: Bitmap) {
        val resizedBitmap = resizeBitmap(bitmap, 500, 500)
        imageList.add(resizedBitmap)

        refreshImageViews()
    }

    private fun refreshImageViews() {
        val imageViewIds = listOf(
            R.id.firstImageView,
            R.id.secondImageView,
            R.id.thirdImageView,
            R.id.fourthImageView,
            R.id.fifthImageView
        )

        // Limpio todos los ImageViews
        for (id in imageViewIds) {
            val imageView = findViewById<ImageView>(id)
            imageView.setImageDrawable(null)
        }

        for (i in imageList.indices) {
            if (i < imageViewIds.size) {
                val imageView = findViewById<ImageView>(imageViewIds[i])
                imageView.setImageBitmap(imageList[i])

                val deleteButtonId = resources.getIdentifier("deleteImageButton${i + 1}", "id", packageName)
                val deleteButton = findViewById<ImageButton>(deleteButtonId)
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    deleteButton.visibility = View.GONE
                    removeImage(i)
                }
            }
        }
    }

    private fun removeImage(position: Int) {
        if (position in imageList.indices) {
            imageList.removeAt(position)
            refreshImageViews()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    //GALERÍA

    // Verifico y solicito permiso para Android 13 (API 33) y superior
    fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_CODE)
            } else {
                openGallery()
            }
        } else {
            // Para versiones anteriores de Android
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Permite solo imágenes
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // Convierte el Uri en un Bitmap
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}