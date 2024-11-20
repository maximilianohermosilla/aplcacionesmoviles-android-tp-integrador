package com.example.android_tp_integrador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.android_tp_integrador.placeholder.PlaceholderContent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance();
    lateinit var selectedRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile);

        val nameInput: EditText = findViewById(R.id.nameInput)
        val lastnameInput: EditText = findViewById(R.id.lastnameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val phoneInput: EditText = findViewById(R.id.phoneInput)

        // RadioGroup y RadioButtons
        val radioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)
        val radioButtonDenunciante = findViewById<RadioButton>(R.id.denuncianteRadioButton)
        val radioButtonProtector = findViewById<RadioButton>(R.id.protectorRadioButton)

        val cancelButton: Button = findViewById(R.id.cancelButton)
        val saveButton: Button = findViewById(R.id.saveButton)

        val preferences: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id: String = preferences.getString("id", null).toString()
        val provider: String = preferences.getString("provider", null).toString()


        lifecycleScope.launch {
            val user = obtenerUsuarioPorId(id)
            if (user != null) {
                println("Usuario encontrado: $user")
                nameInput.setText(user.name)
                lastnameInput.setText(user.lastname)
                emailInput.setText(user.email)
                passwordInput.setText(user.password)
                phoneInput.setText(user.phone)

                when (user.role) {
                    "Denunciante" -> radioGroup.check(radioButtonDenunciante.id)
                    "Protector" -> radioGroup.check(radioButtonProtector.id)
                    else -> radioGroup.clearCheck()
                }

                if(provider == "GOOGLE"){
                    passwordInput.isEnabled = false
                    passwordInput.visibility = View.GONE
                }

            } else {
                println("Usuario no encontrado.")
                //showAlert()
            }
        }

        saveButton.setOnClickListener{
            val selectedRoleButton = radioGroup.checkedRadioButtonId
            if (selectedRoleButton != -1) {  // Verifica que haya un RadioButton seleccionado
                val selectedRadioButton: RadioButton = findViewById(selectedRoleButton)

                selectedRole = selectedRadioButton.text.toString()
                println("Seleccionado: $selectedRole")
            }

            db.collection("users").document(id).set(
                hashMapOf(
                    "id" to id,
                    "name" to nameInput.text.toString(),
                    "lastname" to lastnameInput.text.toString(),
                    "email" to emailInput.text.toString(),
                    "password" to passwordInput.text.toString(),
                    "phone" to phoneInput.text.toString(),
                    "role" to selectedRole
                )
            )

            Toast.makeText(this, "Usuario actualizado exitosamente", Toast.LENGTH_LONG).show()
        }
        
        cancelButton.setOnClickListener {
            onBackPressed()
        }

        setupNavigation()
    }

    suspend fun obtenerUsuarioPorId(id: String): PlaceholderContent.UserItem? {
        return try {
            val documentSnapshot = db.collection("users")
                .document(id)
                .get()
                .await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject( PlaceholderContent.UserItem::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error al obtener el usuario: ${e.message}")
            null
        }
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