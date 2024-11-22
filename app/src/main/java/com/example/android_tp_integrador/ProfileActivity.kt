package com.example.android_tp_integrador

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.android_tp_integrador.placeholder.PlaceholderContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class ProfileActivity : ComponentActivity() {

    private lateinit var languageSpinner: Spinner
    private lateinit var prefs: SharedPreferences
    private var isSpinnerInitialized = false
    private val db = FirebaseFirestore.getInstance();
    lateinit var selectedRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        loadLocale()

        val nameInput: EditText = findViewById(R.id.nameInput)
        val lastnameInput: EditText = findViewById(R.id.lastnameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val phoneInput: EditText = findViewById(R.id.phoneInput)

        val nameTxt: TextView = findViewById(R.id.nameTextView)
        val emailTxt: TextView = findViewById(R.id.emailTextView)
        val roleTxt: TextView = findViewById(R.id.userRoleTextView)

        // RadioGroup y RadioButtons
        val radioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)
        val radioButtonDenunciante = findViewById<RadioButton>(R.id.denuncianteRadioButton)
        val radioButtonProtector = findViewById<RadioButton>(R.id.protectorRadioButton)

        val cancelButton: Button = findViewById(R.id.cancelButton)
        val saveButton: Button = findViewById(R.id.saveButton)
        var logoutButton: Button = findViewById(R.id.logoutButton);

        val preferences: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id: String = preferences.getString("id", null).toString()
        val provider: String = preferences.getString("provider", null).toString()

        // Instancia Spiner
        languageSpinner = findViewById(R.id.languageSpinner)
        setupLanguageSpinner()

        lifecycleScope.launch {
            val user = obtenerUsuarioPorId(id)
            if (user != null) {
                println("Usuario encontrado: $user")
                nameInput.setText(user.name)
                lastnameInput.setText(user.lastname)
                emailInput.setText(user.email)
                passwordInput.setText(user.password)
                phoneInput.setText(user.phone)

                nameTxt.setText("${user.name} ${user.lastname}")
                emailTxt.setText("Email: ${user.email}")
                roleTxt.setText(user.role)

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

        logoutButton.setOnClickListener{
            val currentLanguage = getCurrentLanguage()
            var message = ""
            if (currentLanguage == "en"){
                message = "¿Are you sure you want to log out?"
            } else {
                message = "¿Estás seguro que quieres cerrar sesión?"
            }

            showConfirmationDialog(
                context = this,
                message
            ) { isConfirmed ->
                if (isConfirmed) {
                    val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                    prefs.clear()
                    prefs.apply()

                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, AuthActivity::class.java))
                } else {}
            }

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

    private fun setupLanguageSpinner() {
        // Lista de idiomas con sus respectivas banderas
        val languages = listOf(
            Pair(R.drawable.argentina, "Español"),
            Pair(R.drawable.united_states, "English")
        )

        val adapter = LanguageAdapter(this, languages)
        languageSpinner.adapter = adapter

        // Establece idioma previamente seleccionado
        val savedLanguage = prefs.getString("selectedLanguage", "es")
        languageSpinner.setSelection(if (savedLanguage == "en") 1 else 0)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isSpinnerInitialized) {
                    when (position) {
                        0 -> setLocale("es")
                        1 -> setLocale("en")
                    }
                } else {
                    isSpinnerInitialized = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Si no se selecciona nada, no se hace nada
            }
        }
    }

    private fun setLocale(languageCode: String) {
        val currentLanguage = prefs.getString("selectedLanguage", "")
        if (currentLanguage != languageCode) { // Solo recargar si el idioma es diferente
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)

            // Guardar configuración en SharedPreferences
            with(prefs.edit()) {
                putString("selectedLanguage", languageCode)
                apply()
            }

            // Recargar actividad para aplicar el cambio
            recreate()
        }
    }

    private fun loadLocale() {
        val languageCode = prefs.getString("selectedLanguage", "es")
        val locale = Locale(languageCode ?: "es")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun showConfirmationDialog(
        context: Context,
        message: String,
        onResult: (Boolean) -> Unit
    ) {
        val dialog = AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Sí") { _, _ ->
                onResult(true)
            }
            .setNegativeButton("No") { _, _ ->
                onResult(false)
            }
            .create()

        dialog.show()
    }

    private fun getCurrentLanguage(): String {
        return prefs.getString("selectedLanguage", "es") ?: "es"
    }
}