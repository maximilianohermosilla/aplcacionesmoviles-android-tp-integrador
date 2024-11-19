package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NuevaDenunciaActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance();

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_denuncia);
        setupNavigation()

        val fechaActual = LocalDateTime.now()
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val fechaFormateada = fechaActual.format(formato)

        val prefs: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val userId: String? = prefs.getString("id", null)
        var tituloEditText: EditText = findViewById(R.id.tituloEditText);
        var descripcionEditText: EditText = findViewById(R.id.descripcionEditText);

        val cancelarButton: Button = findViewById(R.id.cancelarButton)
        cancelarButton.setOnClickListener {
            onBackPressed()
        }

        var nextButton: Button = findViewById(R.id.siguienteButton);
        nextButton.setOnClickListener {
            val uuid: UUID = UUID.randomUUID();
            db.collection("denuncias").document(uuid.toString()).set(
                hashMapOf(
                    "id" to uuid.toString(),
                    "dateCreation" to fechaFormateada.toString(),
                    "title" to tituloEditText.text.toString(),
                    "description" to descripcionEditText.text.toString(),
                    "state" to "Pendiente",
                    "userCreation" to userId.toString()
                )
            )
            val intent = Intent(this, PhotoActivity::class.java)
            //startActivity(intent)

            showNext(uuid.toString());
        }
    }

    private fun showNext(id: String){
        val nextIntent = Intent(this, PhotoActivity::class.java).apply{
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