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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : ComponentActivity() {
    val db = FirebaseFirestore.getInstance();

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