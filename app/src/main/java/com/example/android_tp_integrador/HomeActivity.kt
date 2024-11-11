package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home);

        val bundle: Bundle? = intent.extras
        val id: String = bundle?.getString("id").toString()
        val email: String = bundle?.getString("email").toString()
        val provider: String = bundle?.getString("provider").toString()
        val name: String = bundle?.getString("name").toString()

        setup(id ?: "", email ?: "", provider ?: "", name ?: "");

        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("id", id)
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.putString("name", name)
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


    }

    private fun setup(id: String, email: String, provider: String, name: String) {
        title = "Inicio"
        var emailTextView: TextView = findViewById(R.id.emailTextView);
        var providerTextView: TextView = findViewById(R.id.providerTextView);
        var nameTextView: TextView = findViewById(R.id.nameTextView);
        var logoutButton: Button = findViewById(R.id.logoutButton);

        emailTextView.text = email;
        providerTextView.text = "$provider $id";
        nameTextView.text = "Bienvenido/a $name";

        logoutButton.setOnClickListener{

            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

    }
}