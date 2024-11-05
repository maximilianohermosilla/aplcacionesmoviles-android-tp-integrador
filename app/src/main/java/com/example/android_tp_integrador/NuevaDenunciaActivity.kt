package com.example.android_tp_integrador

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class NuevaDenunciaActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_denuncia);

        val cancelarButton: Button = findViewById(R.id.cancelarButton)
        val siguienteButton: Button = findViewById(R.id.siguienteButton)
        var tituloEditText: EditText = findViewById(R.id.tituloEditText);
        var descripcionEditText: EditText = findViewById(R.id.descripcionEditText);

        cancelarButton.setOnClickListener {
            onBackPressed()
        }

        siguienteButton.setOnClickListener {
            db.collection("denuncias").document(tituloEditText.text.toString()).set(
                hashMapOf("titulo" to tituloEditText.text.toString(),
                    "descripcion" to descripcionEditText.text.toString())
            )
            Toast.makeText(this@NuevaDenunciaActivity,
                ("Denuncia '" + tituloEditText.text.toString() + "' cargada correctamente"), Toast.LENGTH_LONG).show();

            tituloEditText.text.clear();
            //descripcionEditText.text.clear();
            descripcionEditText.setText("");
        }
    }
}