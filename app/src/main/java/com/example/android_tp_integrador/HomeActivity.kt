package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
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

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : ComponentActivity() {
    lateinit var userToken: String // Token del mismo usuario
    //private val userToken = "TU_TOKEN_DE_FCM" // Token del dispositivo del usuario al que se le quiere enviar la notificación
    private val bearerToken = "ya29.c.c0ASRK0Gap4sxqQZ8Y9w_5Z0oeZkKS6kFVDbGWq8fnK1zAzfzo2U3sbsXjNnbSs-taKnviq651HNuxxlAeN1yprYqTGMSIBtg3OUUUErqy98awuTe83ZVJg9wHxMn2LiezDgSpS9EaMXyj4AcbkAx5dvzU4Obv9kfSlqaqhGk4Vl2RqoXqm9BlH-BXGXgNYVF5TSy1O7PySyXhcOTOOl-vi2xSQPUuyICPb506B3UkmHKmEp93xN7-NS7KXBhU5c5nHU5ycEC9UYEgEpXcYxlVGVWr8MDc9gFk_WH3oYg4a_jIC4pjzAB59ZGanUxfjhoTizDCCsZuQe0AvxsPUdpaCYrBZgZIwbIhzS-cdImnjFSJC0TlocewWeW_BQG387P1vrY_0dqJyyeq2Jl5lnJ7bMzMMU1t_9zya8ZbSh0fxsh1ZFlVIoWt3xWXjtkg7ZVnMvMJSlbeyvxlW3bm_ftyF1rVvomrdm1obf8W8fnWkig1I08apBczmvhfJdwtjldc42nInSOb0nQnc3-75vYcxvXbVjRX-8Mdohtly1WJdJVxMq8aJcZycbrppkghBgwpQURB4l-cqYiWq6MVsM6vcSgzBoo3UFVvt5nkR1FU93j5jFfm2uxryMcoQ9QqOuyShutSk7xMfa5FF-1wt8_UmYph6r8ySs3jsu9eu6dtrpt290rI1F1McJlt959JwQu0Zzk0MIRRjVFQBnhuYiQdmd8FyMd393epF8vkotBJu2OfU5dwpo93gtx0zfjjv9xRSJOihBemulz9r1vMoJSXY--4XuMU_BvRh1x2UZd1iaiy2W3bb9foqhexX4ir9-nU-gViniose9Y1oSUgm7_3yelpsvfQlQWUnljV5I1gb5s482_ZrQVM35-bXRiX4zh_tek51VeFxhaMaBOV9XSl9uB4yw83SRn3ByBU-jXave8zf_p6y_5BQvZqUp_Jy-ic-Ote_XfIkqWMoS93z73y2d5xVx3Z0ZBnqubVzuo54I9pRni4QYybv77" // Reemplaza con tu token válido generado previamente

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home);

        val bundle: Bundle? = intent.extras
        val id: String = bundle?.getString("id").toString()
        val email: String = bundle?.getString("email").toString()
        val provider: String = bundle?.getString("provider").toString()
        val name: String = bundle?.getString("name").toString()
        val role: String = bundle?.getString("role").toString()

        setup(id ?: "", email ?: "", provider ?: "", name ?: "", role ?: "");

        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("id", id)
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.putString("name", name)
        prefs.putString("role", role)
        prefs.apply()

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
        var emailTextView: TextView = findViewById(R.id.emailTextView);
        var providerTextView: TextView = findViewById(R.id.providerTextView);
        var nameTextView: TextView = findViewById(R.id.nameTextView);
        var logoutButton: Button = findViewById(R.id.logoutButton);

        emailTextView.text = email;
        providerTextView.text = "$provider $id";
        nameTextView.text = "Bienvenido/a $name ($role)";

        logoutButton.setOnClickListener{

            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
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

}