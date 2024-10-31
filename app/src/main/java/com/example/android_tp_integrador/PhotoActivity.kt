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
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout

class PhotoActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private val STORAGE_PERMISSION_CODE = 103

    private lateinit var imageContainer: LinearLayout

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

        // Inicializar el contenedor de imágenes
        imageContainer = findViewById(R.id.imageContainer)

        var openCameraBtn: Button = findViewById(R.id.openCameraButton)
        openCameraBtn.setOnClickListener {
            checkCameraPermission()
        }

        // Botón para abrir la galería
        val openGalleryBtn: Button = findViewById(R.id.openGaleryButton) // Asegúrate de tener este botón en tu layout
        openGalleryBtn.setOnClickListener {
            checkStoragePermission()
        }
    }

    // Método para verificar y solicitar permisos
    fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar el permiso si no está concedido
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            // Si el permiso ya está concedido, abrir la cámara
            openCamera()
        }
    }

    // Abrir la cámara
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Manejar el resultado de los permisos solicitados
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

    // Manejar el resultado de la cámara o galería
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // Aquí manejarías la foto tomada con la cámara (podemos manejarlo más adelante)
                    if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
                        val imageBitmap = data?.extras?.get("data") as Bitmap
                        addImageToLayout(imageBitmap)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        // Convertir el URI a Bitmap
                        val bitmap = uriToBitmap(selectedImageUri)
                        if (bitmap != null) {
                            addImageToLayout(bitmap)
                        }
                    }
                }
            }
        }
    }

    // Agregar la imagen al layout dinámicamente
//    private fun addImageToLayout(bitmap: Bitmap) {
//        val imageView = ImageView(this)
//        imageView.setImageBitmap(bitmap)
//
//        // Configurar tamaño y márgenes del ImageView
//        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        params.setMargins(8, 8, 8, 8)  // Márgenes para separar las imágenes
//        imageView.layoutParams = params
//
//        // Agregar la imagen al contenedor
//        imageContainer.addView(imageView)
//    }

    // Agregar la imagen al layout dinámicamente
//    private fun addImageToLayout(bitmap: Bitmap) {
//        val imageView = ImageView(this)
//        imageView.setImageBitmap(bitmap)
//
//        // Configurar tamaño de la imagen y otras propiedades si es necesario
//        val params = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        params.setMargins(8, 8, 8, 8) // Margen entre imágenes
//        imageView.layoutParams = params
//
//        // Añadir el ImageView al contenedor
//        imageContainer.addView(imageView)
//    }

    private fun addImageToLayout(bitmap: Bitmap) {
        val imageView = ImageView(this)

        // Redimensionar el bitmap si es necesario
        val resizedBitmap = resizeBitmap(bitmap, 500, 500) // Máximo 500px de ancho y alto

        // Establecer el bitmap redimensionado en el ImageView
        imageView.setImageBitmap(resizedBitmap)

        // Configurar tamaño de la imagen y otras propiedades si es necesario
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 8, 8, 8) // Margen entre imágenes
        imageView.layoutParams = params

        // Añadir el ImageView al contenedor
        imageContainer.addView(imageView)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val aspectRatio: Float = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (newHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    //GALERÍA

    // Método para verificar y solicitar permisos de almacenamiento
//    fun checkStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            // Solicitar el permiso si no está concedido
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
//        } else {
//            // Si el permiso ya está concedido, abrir la galería
//            openGallery()
//        }
//    }

    // Verificar y solicitar permiso para Android 13 (API 33) y superior
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

    // Abrir la galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Permitir solo imágenes
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun enableEdgeToEdge() {
        // Método para ajustar la pantalla a modo "Edge to Edge"
        // Puedes dejarlo como está si ya lo tienes implementado
    }

    // Convertir el Uri en un Bitmap
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