package com.example.android_tp_integrador

import android.content.Context
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
        val email: String = bundle?.getString("email").toString()
        val provider: String = bundle?.getString("provider").toString()

        setup(email ?: "", provider ?: "");

        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    private fun setup(email: String, provider: String) {
        title = "Inicio"
        var emailTextView: TextView = findViewById(R.id.emailTextView);
        var providerTextView: TextView = findViewById(R.id.providerTextView);
        var logoutButton: Button = findViewById(R.id.logoutButton);

        emailTextView.text = email;
        providerTextView.text = provider;

        logoutButton.setOnClickListener{

            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

    }
}