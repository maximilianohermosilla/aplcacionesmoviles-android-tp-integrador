package com.example.android_tp_integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class NuevaDenunciaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_denuncia);

        val cancelarButton: Button = findViewById(R.id.cancelarButton)
        cancelarButton.setOnClickListener {
            onBackPressed()
        }

        var nextButton: Button = findViewById(R.id.siguienteButton);
        nextButton.setOnClickListener {
            val intent = Intent(this, PhotoActivity::class.java)
            startActivity(intent)
        }
    }
}