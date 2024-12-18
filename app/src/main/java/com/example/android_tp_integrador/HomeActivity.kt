package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : ComponentActivity() {
    val db = FirebaseFirestore.getInstance();
    var role: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home);

        val preferences: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        val bundle: Bundle? = intent.extras

        val id: String = preferences.getString("id", null) ?: bundle?.getString("id").toString()
        val email: String = preferences.getString("email", null) ?: bundle?.getString("email").toString()
        val provider: String = preferences.getString("provider", null) ?: bundle?.getString("provider").toString()
        val name: String = preferences.getString("name", null) ?: bundle?.getString("name").toString()
        val lastname: String = preferences.getString("lastname", null) ?: bundle?.getString("lastname").toString()
        role = preferences.getString("role", null) ?: bundle?.getString("role").toString()

        setup(id ?: "", email ?: "", provider ?: "", name ?: "", role ?: "");

        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("id", id)
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.putString("name", name)
        prefs.putString("lastname", lastname)
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

        nameTextView.text = "Bienvenido/a $name ($role)";

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
                    if(role == "Protector"){
                        Toast.makeText(this, "Esta opción no está habilitada para tu perfil de usuario", Toast.LENGTH_LONG).show()
                    }
                    else{
                        startActivity(Intent(this, NuevaDenunciaActivity::class.java))
                    }
                    true
                }
                R.id.nav_user -> {
                    println("nav_user presionado")
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}