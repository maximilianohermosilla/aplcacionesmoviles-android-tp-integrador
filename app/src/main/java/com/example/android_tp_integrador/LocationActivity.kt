package com.example.android_tp_integrador

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android_tp_integrador.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
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
    private var addressFull: String? = ""
    private var addressGeneral: String? = ""
    private var latLng: LatLng? = null
    private var currentMarker: Marker? = null

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
            //Guardado de ubicación (mover a botón)
            if(latLng != null){
                db.collection("denuncias").document(uuid).set(hashMapOf(
                    "ubication" to latLng.toString().replace("lat/lng: (", "").replace(")", ""),
                    "address" to addressFull,
                    "addressCity" to addressGeneral,
                ), SetOptions.merge())
            }
            // Abrir otra actividad (a definir)
            val intent = Intent(this, DenunciaDetailHostActivity::class.java)
            startActivity(intent)
            // startActivity(Intent(this, SiguienteActivity::class.java))
        }

        setupNavigation()
        println("Maps inicializado")
    }

    private fun searchLocation(address: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            // Obtiene una lista de direcciones que coincidan con el texto ingresado
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                latLng = LatLng(location.latitude, location.longitude)

                fetchAddressFromLatLng(latLng!!)
                // Mueve la cámara a la ubicación encontrada y añade un marcador
                googleMap.clear()  // Limpia los marcadores anteriores
                googleMap.addMarker(MarkerOptions().position(latLng!!).title(address))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng!!, 15f))
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

        googleMap.setOnMapClickListener { latLng ->
            addMarkerAtPosition(latLng)
        }

        println("on Map Ready finalizado")
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

    private fun addMarkerAtPosition(position: LatLng) {
        // Borra el marcador anterior si existe
        currentMarker?.remove()

        // Crea un nuevo marcador en la posición seleccionada
        currentMarker = googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("Seleccionado")
                .snippet("Lat: ${position.latitude}, Lng: ${position.longitude}")
        )
        latLng = LatLng(position.latitude, position.longitude)

        fetchAddressFromLatLng(latLng!!)
        // Centra el mapa en la posición seleccionada
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    private fun fetchAddressFromLatLng(position: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            // Obtén una lista de direcciones (el primer resultado suele ser el más preciso)
            val addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0) // Dirección completa
                val locality = address.locality // Localidad o ciudad
                val province = address.adminArea // Provincia
                val country = address.countryName // País
                val postalCode = address.postalCode // Código postal
                addressFull =  fullAddress;
                addressGeneral = "$locality, $province"

                // Mostrar la dirección en un Toast (puedes usar otros métodos para mostrarla)
                Toast.makeText(
                    this,
                    "Dirección: $fullAddress\nCiudad: $locality\nProvincia: $province\nPaís: $country\nCP: $postalCode",
                    Toast.LENGTH_LONG
                ).show()

                // También puedes usar estos datos para actualizar el marcador
                currentMarker?.snippet = fullAddress
                currentMarker?.showInfoWindow()
            } else {
                Toast.makeText(this, "No se encontró la dirección para esta ubicación.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al obtener la dirección: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation(){
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
                    startActivity(Intent(this, ProfileActivity::class.java))
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