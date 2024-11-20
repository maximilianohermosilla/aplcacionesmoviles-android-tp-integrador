package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : ComponentActivity() {
    lateinit var userToken: String // Token del mismo usuario
    //private val userToken = "TU_TOKEN_DE_FCM" // Token del dispositivo del usuario al que se le quiere enviar la notificación
    // Reemplaza con tu token válido generado previamente
    private val bearerToken = "eqvcSOddSLWNm9shHL8Ytm:APA91bH2DHCL996vwe-_fVTqLSQUYZoPyZu0hKUnAytgBcmYoS836Yve1L-RqawaiuMx0-9iW16u1vLEgNHOl6eut1MKDI9bZsYo-ZRVpuKSGYF8Md8f0HU"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home);

        val bundle: Bundle? = intent.extras

        println("Obteniendo información de usuario")
        val preferences: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)


        val id: String = preferences.getString("id", null) ?: bundle?.getString("id").toString()
        val email: String = preferences.getString("email", null) ?: bundle?.getString("email").toString()
        val provider: String = preferences.getString("provider", null) ?: bundle?.getString("provider").toString()
        val name: String = preferences.getString("name", null) ?: bundle?.getString("name").toString()
        val role: String = preferences.getString("role", null) ?: bundle?.getString("role").toString()


        if(id == "null"){
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        println("Información de usuario obtenida")
        setup(id ?: "", email ?: "", provider ?: "", name ?: "", role ?: "");


        println("Actualizando SharedPreferences con información de usuario")
        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("id", id)
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.putString("name", name)
        prefs.putString("role", role)
        prefs.apply()


        println("Verificando rol de usuario")
        if(role == "Protector"){
            var nuevaDenunciaButton: Button = findViewById(R.id.nuevaDenunciaButton);
            nuevaDenunciaButton.visibility = View.GONE
        }

        var denunciasButton: Button = findViewById(R.id.denunciasButton);
        denunciasButton.setOnClickListener {
            val intent = Intent(this, DenunciaDetailHostActivity::class.java)
            startActivity(intent)
        }

        var nuevaDenunciaButton: Button = findViewById(R.id.nuevaDenunciaButton);
        nuevaDenunciaButton.setOnClickListener {
            val intent = Intent(this, NuevaDenunciaActivity::class.java)
            startActivity(intent)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userToken = task.result
                Log.d("FCM Token", "Token obtenido manualmente: $userToken")
            } else {
                Log.e("FCM Token", "Error al obtener el token", task.exception)
            }
        }

        val sendNotificationButton: Button = findViewById(R.id.sendNotificationButton)

        sendNotificationButton.setOnClickListener {
            sendNotificationToSelf(userToken)
        }

    }

    private fun setup(id: String, email: String, provider: String, name: String, role: String) {
        title = "Inicio"
        var nameTextView: TextView = findViewById(R.id.nameTextView);
        var logoutButton: Button = findViewById(R.id.logoutButton);

        if(id == null || id == "null"){
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        nameTextView.text = "Bienvenido/a $name ($role)";

        logoutButton.setOnClickListener{

            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        setupNavigation()
    }

    private fun sendNotificationToSelf(token: String) {
        // URL para la API de FCM
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/62704752521/messages:send"

        // Creación del payload del mensaje
        val notificationPayload = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token)
                put("data", JSONObject().apply {
                    put("title", "Notificación de prueba")
                    put("body", "¡Esto es una notificación enviada a ti mismo!")
                })
            })
        }

        // Creación solicitud HTTP
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            fcmUrl,
            notificationPayload,
            Response.Listener { response ->
                Log.d("FCM", "Notificación enviada con éxito: $response")
            },
            Response.ErrorListener { error ->
                Log.e("FCM", "Error al enviar la notificación: ${error.message}")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $bearerToken"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Enviar la solicitud
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
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