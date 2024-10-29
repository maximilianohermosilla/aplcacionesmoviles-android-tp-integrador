package com.example.android_tp_integrador

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
    }
}