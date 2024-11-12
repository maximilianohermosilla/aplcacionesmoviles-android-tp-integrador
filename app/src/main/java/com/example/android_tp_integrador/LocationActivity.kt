package com.example.android_tp_integrador

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.health.connect.datatypes.ExerciseRoute
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android_tp_integrador.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private val db = FirebaseFirestore.getInstance();
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val locationPermissionRequestCode = 1
    private lateinit var uuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle: Bundle? = intent.extras
        uuid = bundle?.getString("id").toString()

        println("Inicializando maps")
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_location)

        // Inicializa el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Configura el botón de búsqueda
        val previousButton: Button = findViewById(R.id.previousButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val etAddress = findViewById<EditText>(R.id.textUbication)

        btnSearch.setOnClickListener {
            val address = etAddress.text.toString()
            if (address.isNotEmpty()) {
                searchLocation(address)
            } else {
                Toast.makeText(this, "Por favor ingresa una dirección", Toast.LENGTH_SHORT).show()
            }
        }

        previousButton.setOnClickListener {
            // Volver a la actividad anterior
            //finish()
        }

        nextButton.setOnClickListener {
            // Abrir otra actividad (a definir)
            // startActivity(Intent(this, SiguienteActivity::class.java))
        }

        println("Maps inicializado")

        // Configurar botones
//        val previousButton: Button = findViewById(R.id.previousButton)
//        val nextButton: Button = findViewById(R.id.nextButton)
//
//        previousButton.setOnClickListener {
//            // Volver a la actividad anterior
//            //finish()
//        }
//
//        nextButton.setOnClickListener {
//            // Abrir otra actividad (a definir)
//            // startActivity(Intent(this, SiguienteActivity::class.java))
//        }

        // Pedir permisos de ubicación
//        requestLocationPermission()
    }

    private fun searchLocation(address: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            // Obtiene una lista de direcciones que coincidan con el texto ingresado
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)

                //Guardado de ubicación (mover a botón)
                db.collection("denuncias").document(uuid).set(hashMapOf(
                    "ubication" to latLng
                ), SetOptions.merge())
                // Mueve la cámara a la ubicación encontrada y añade un marcador
                googleMap.clear()  // Limpia los marcadores anteriores
                googleMap.addMarker(MarkerOptions().position(latLng).title(address))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        println("Inicializando onMapReady")
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Habilita la ubicación si el permiso ha sido concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            // Solicita el permiso de ubicación
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
        }
        // Agrega un marcador en una ubicación específica (ej. Sidney) y mueve la cámara
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))

        //googleMap.uiSettings.isZoomControlsEnabled = true
        //googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        println("on Map Ready finalizado")
        // Verificar si ya se concedieron permisos de ubicación
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            googleMap.isMyLocationEnabled = true
//            getCurrentLocation()
//        } else {
//            requestLocationPermission()
//        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Si ya tiene permisos, obtener la ubicación
            getCurrentLocation()
        } else {
            // Pedir permisos
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Si el permiso es concedido, obtener la ubicación
            getCurrentLocation()
        } else {
            // Manejar el caso en que el permiso no es concedido
            // Mostrar un mensaje al usuario o deshabilitar la funcionalidad
        }
    }

//    private fun getCurrentLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//                if (location != null) {
//                    val userLocation = LatLng(location.latitude, location.longitude)
//                    googleMap.addMarker(MarkerOptions().position(userLocation).title("Tu ubicación"))
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
//                }
//            }
//        }
//    }
    // Obtiene la ubicación actual del usuario
    private fun getCurrentLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Usa la ubicación actual para centrar el mapa
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación actual"))
                }
            }
        }
    }

    // Maneja la respuesta de solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Si se concede el permiso, habilita la ubicación en el mapa y obtiene la ubicación actual
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                    getCurrentLocation()
                }
            } else {
                // Muestra un mensaje si el permiso es denegado
                // Puedes mostrar un diálogo aquí si deseas explicar por qué necesitas el permiso
            }
        }
    }
}