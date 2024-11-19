package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.transition.Visibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
//import okhttp3.Request
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
    private val bearerToken = "ya29.c.c0ASRK0Gag0ZMykdzFa maelFQteTnvHKNaGM0szVLifT15ef2gIiMO59Q-l2UiiJK90C8cjHqYxmt0snpz7o0leWvAFK3BCBbpzPh1_QENFHUMvMi08pK96MctE0_iiYMb03JMl1YmZ7UyzMW0Le7C5I_r0msyml7I2n0GeCSwyJtsSOuzJcMPAw67nWzFRx8mbTyOf4vyTivM4701noDU1HZuZv-ofOz8l7qAWE0g54IkBU8QgfUWBbuaOG_26Uwdil2GuxQpdxsFIMCgsH0OGqBwpBN4vo1ExUw4yalZ2-MuXu1gSuEPjbkMnsGI1G12gFSlB_PBEhurUc2bZYT05YnOUQ9NNBKf_wYAlrSRmtO5pxSkVfY3BJPG_ZUgE387CZmZ1mIwhz7SWz5aM1Xl7cQ59fMhock4mu9y6woZ0e5fBMyqlOWnpQlRpeYqenXFgRXlwFZnIi8zwdJYa7nvcXip6eOo1z-YYRBpk5UmdeUYRvBM0lxzgjF4JMBtg98c-OZrkkOsmpihya9dftQfooiiqwZ290vuVfna5MYBgVq0cqhIsOl25pV_fyWlrIiUcycb-Oe2Yaw-w8zZZhr2uJ6tW3wIBjOYWdgyyOrpBtVs18b9y123ftfOxgsYv_Ba7j7-rnQXa9-rcI7ZIqk_iSidcn0Ywydv1p2VYIojs4pWMWkWWeaYtd6jmr-rJ6yU_Ml_aqIaq1pVnJmJRSkzrI5kfRIqc-4z2wF9kjlbmiUzfwo4tuu7xniXM6SJ8U92QOWjgZV1mFfx59OZWU2i_QxoXS4FZU20uBnhdIof__v73Ivg5epFaFffrt1-Xintdwzb75kjcxUQX767t8IJnVkpObfbjxu-W6sS14unz2oi9-kXS-msXJpVj9nkroqtJ96nw9Ywaai9m67W1Xiu4IYcr2rqsOtr436Bray6n9g1Zz2FbpzrbQ-qeoewVB8wQweoItMnqaM-zeUFyp_1uYcr_Z_R_4vzrFqmUW6Ovoda2681tQqwOc87f"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home);

        val preferences: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        val bundle: Bundle? = intent.extras

        val id: String = preferences.getString("id", null) ?: bundle?.getString("id").toString()
        val email: String = preferences.getString("email", null) ?: bundle?.getString("email").toString()
        val provider: String = preferences.getString("provider", null) ?: bundle?.getString("provider").toString()
        val name: String = preferences.getString("name", null) ?: bundle?.getString("name").toString()
        val role: String = preferences.getString("role", null) ?: bundle?.getString("role").toString()

        setup(id ?: "", email ?: "", provider ?: "", name ?: "", role ?: "");

        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("id", id)
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.putString("name", name)
        prefs.putString("role", role)
        prefs.apply()

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

        nameTextView.text = "Bienvenido/a $name ($role)";

        logoutButton.setOnClickListener{

            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
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