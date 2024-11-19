package com.example.android_tp_integrador

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import java.util.ArrayList

class PhotoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance();
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private val STORAGE_PERMISSION_CODE = 103

    private lateinit var imageContainer: ConstraintLayout
    private lateinit var logoImage: ImageView
    private val imageList = mutableListOf<Bitmap>()
    private val uriImageList: MutableList<String> = ArrayList()
    private var cloudinary_name: String = "";
    private var cloudinary_api_key: String = "";
    private var cloudinary_api_secret: String = "";


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_photo)
        setupNavigation()

        val remoteConfigs: FirebaseRemoteConfig = Firebase.remoteConfig.apply{
            setConfigSettingsAsync(remoteConfigSettings {minimumFetchIntervalInSeconds = 60})
            fetchAndActivate()
        }

        remoteConfigs.fetch(0)
        remoteConfigs.activate()
        cloudinary_name = remoteConfigs.getString("cloudinary_name")
        cloudinary_api_key = remoteConfigs.getString("cloudinary_api_key")
        cloudinary_api_secret = remoteConfigs.getString("cloudinary_api_secret")

        val config = mapOf(
            "cloud_name" to cloudinary_name,
            "api_key" to cloudinary_api_key,
            "api_secret" to cloudinary_api_secret
        )

        MediaManager.init(this, config)
//        val requestId: String = MediaManager.get().upload("dog.mp4")
//            .unsigned("preset1")
//            .option("resource_type", "video")
//            .option("asset_folder", "pets/dogs/")
//            .option("public_id", "my_dog")
//            .option("notification_url", "https://mysite.example.com/notify_endpoint")
//            .dispatch()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.photoActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bundle: Bundle? = intent.extras
        val uuid: String = bundle?.getString("id").toString()
        imageContainer = findViewById(R.id.imageContainer)
        logoImage = findViewById(R.id.logoImage)

        var openCameraBtn: Button = findViewById(R.id.openCameraButton)
        openCameraBtn.setOnClickListener {
            checkCameraPermission()
        }

        val openGalleryBtn: Button = findViewById(R.id.openGaleryButton)
        openGalleryBtn.setOnClickListener {
            checkStoragePermission()
        }

        val previousButton: Button = findViewById(R.id.previousBtn)
        previousButton.setOnClickListener {
            onBackPressed()
        }

        var nextButton: Button = findViewById(R.id.nextBtn);
        nextButton.setOnClickListener {
            //startActivity(intent)
            db.collection("denuncias").document(uuid).set(hashMapOf(
                "images" to uriImageList
            ), SetOptions.merge())
            showNext(uuid.toString());
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
        if (intent.resolveActivity(this.packageManager) != null) {
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
                        data?.data?.let { uri ->
                            subirImagen(uri)  // Llamamos a la función para subir la imagen
                        }
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

        logoImage.visibility = View.GONE;

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

    fun subirImagen(uri: Uri) {
        MediaManager.get().upload(uri)
            .option("asset_folder", "aplicacionesmoviles/")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Proceso de subida iniciado
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Puedes actualizar una barra de progreso si es necesario
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Subida exitosa, obtener la URL de la imagen
                    val imageUrl = resultData["url"] as String
                    uriImageList.add(imageUrl)
                    println("URL de la imagen: $imageUrl")
                }

                override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                    // Manejo de error en la subida
                    println("Error al subir la imagen: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                    // Subida reprogramada en caso de error
                }
            }).dispatch()
    }

    private fun showNext(id: String){
        val nextIntent = Intent(this, LocationActivity::class.java).apply{
            putExtra("id", id)
        }

        startActivity(nextIntent)
    }

    fun setupNavigation(){
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    println("Home presionado")
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_list -> {
                    println("nav_list presionado")
                    startActivity(Intent(this, DenunciaDetailHostActivity::class.java))
                    true
                }
                R.id.nav_notification -> {
                    println("nav_notification presionado")
                    startActivity(Intent(this, NuevaDenunciaActivity::class.java))
                    true
                }
                R.id.nav_user -> {
                    println("nav_user presionado")
                    startActivity(Intent(this, HomeActivity::class.java))
//                    if (this !is NotificationsActivity) {
//                        startActivity(Intent(this, NotificationsActivity::class.java))
//                    }
                    true
                }
                else -> false
            }
        }
    }
}