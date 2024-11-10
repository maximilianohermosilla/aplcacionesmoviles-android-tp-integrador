package com.example.android_tp_integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class NuevaDenunciaActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_denuncia);

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
                hashMapOf("titulo" to tituloEditText.text.toString(),
                    "descripcion" to descripcionEditText.text.toString())
            )
            val intent = Intent(this, PhotoActivity::class.java)
            startActivity(intent)
        }
    }
}