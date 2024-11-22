package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.android_tp_integrador.placeholder.PlaceholderContent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NuevaDenunciaActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance();

    var selectedPriorityButton: Int = -1
    lateinit var selectedPriority: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_denuncia);
        setupNavigation()

        val bundle: Bundle? = intent.extras
        var uuid: String = bundle?.getString("uuid").toString()

        val fechaActual = LocalDateTime.now()
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val fechaFormateada = fechaActual.format(formato)

        val prefs: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val userId: String? = prefs.getString("id", null)

        var tituloEditText: EditText = findViewById(R.id.tituloEditText);
        var descripcionEditText: EditText = findViewById(R.id.descripcionEditText);
        val radioGroup: RadioGroup = findViewById(R.id.priorityRadioGroup)
        val bajaRadioButton = findViewById<RadioButton>(R.id.bajaRadioButton)
        val mediaRadioButton = findViewById<RadioButton>(R.id.mediaRadioButton)
        val altaRadioButton = findViewById<RadioButton>(R.id.altaRadioButton)

        if(uuid != ""){
            db.collection("denuncias")
                .document(uuid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Convierte el documento a un objeto PlaceholderItem
                        val denuncia =
                            documentSnapshot.toObject(PlaceholderContent.PlaceholderItem::class.java)

                        // Procesa el objeto obtenido
                        if (denuncia != null) {
                            println("Denuncia encontrada: $denuncia")
                            denuncia?.let {
                                tituloEditText.setText(it.title)
                                descripcionEditText.setText(it.description)

                                when (it.priority) {
                                    "Baja" -> radioGroup.check(bajaRadioButton.id)
                                    "Media" -> radioGroup.check(mediaRadioButton.id)
                                    "Alta" -> radioGroup.check(altaRadioButton.id)
                                    else -> radioGroup.clearCheck()
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener la denuncia: ${exception.message}")
                }
        }

        val cancelarButton: Button = findViewById(R.id.cancelarButton)
        cancelarButton.setOnClickListener {
            onBackPressed()
        }

        var nextButton: Button = findViewById(R.id.siguienteButton);
        nextButton.setOnClickListener {

            // Validación: verificar si los campos están vacíos
            if (tituloEditText.text.isEmpty()) {
                tituloEditText.error = "El título es obligatorio"
                return@setOnClickListener
            }

            if(uuid == null || uuid == "" || uuid == "null") {
                val uuidNew: UUID = UUID.randomUUID();
                uuid = uuidNew.toString();
            }

            val radioGroup: RadioGroup = findViewById(R.id.priorityRadioGroup)
            var selectedPriorityButton = radioGroup.checkedRadioButtonId
            if (selectedPriorityButton != -1) {  // Verifica que haya un RadioButton seleccionado
                val selectedRadioButton: RadioButton = findViewById(selectedPriorityButton)

                selectedPriority = selectedRadioButton.text.toString()
                println("Seleccionado: $selectedPriority")
            }

            db.collection("denuncias").document(uuid.toString()).set(
                hashMapOf(
                    "id" to uuid,
                    "dateCreation" to fechaFormateada.toString(),
                    "title" to tituloEditText.text.toString(),
                    "description" to descripcionEditText.text.toString(),
                    "state" to "Pendiente",
                    "priority" to selectedPriority,
                    "userCreation" to userId.toString()
                ), SetOptions.merge())

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
        bottomNavigationView.selectedItemId = R.id.nav_notification
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