package com.example.android_tp_integrador

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.util.Log
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import com.example.android_tp_integrador.placeholder.PlaceholderContent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val GOOGLE_SIGN_IN = 100
    private val db = FirebaseFirestore.getInstance();
    private lateinit var languageSpinner: Spinner
    private lateinit var prefs: SharedPreferences
    private var isSpinnerInitialized = false
    var flag: Boolean = true
    var selectedRoleButton: Int = -1
    lateinit var selectedRole: String
    lateinit var userToken: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se carga el idioma previamente seleccionado
        prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        loadLocale()

        setContentView(R.layout.activity_auth);

        auth = Firebase.auth

        setup()
        session()

        // Referencia a los elementos del UI
        val expandableBlock: LinearLayout = findViewById(R.id.expandableBlock)
        val registerButton: Button = findViewById(R.id.registerButton)
        val textLogo: TextView = findViewById(R.id.textLogo)
        val loginButton: Button = findViewById(R.id.loginButton)
        val nameInput: EditText = findViewById(R.id.nameInput)
        val lastnameInput: EditText = findViewById(R.id.lastnameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val rePasswordInput: EditText = findViewById(R.id.rePasswordInput)
        val roleTextView: TextView = findViewById(R.id.roleTextView)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val logInButton: Button = findViewById(R.id.logInButton)
        val backButton: Button = findViewById(R.id.backButton)
        val textDisplay: TextView = findViewById(R.id.textDisplay)
        var googleButton: Button = findViewById(R.id.googleButton);
        val radioGroup: RadioGroup = findViewById(R.id.roleRadioGroup)

        registerButton.setOnClickListener {
            flag = true
            expandBlock(expandableBlock, 0.68f)
            removeButtonsAndShowForm(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, signUpButton, backButton, googleButton)
        }

        loginButton.setOnClickListener {
            flag = false
            expandBlock(expandableBlock, 0.55f)
            removeButtonsAndShowForm(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, logInButton, backButton, googleButton)
        }

        backButton.setOnClickListener {

            // Limpio el contenido de los inputs
            nameInput.setText("")
            lastnameInput.setText("")
            emailInput.setText("")
            passwordInput.setText("")
            rePasswordInput.setText("")

            // Limpio posibles errores
            nameInput.error = null
            lastnameInput.error = null
            emailInput.error = null
            passwordInput.error = null
            rePasswordInput.error = null

            if(!flag){
                collapseBlock(expandableBlock, 0.55f, 0.35f)
                restoreInitialState(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, logInButton, backButton, googleButton)
            } else {
                collapseBlock(expandableBlock, 0.72f, 0.35f)
                restoreInitialState(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, signUpButton, backButton, googleButton)
            }
        }

        // Instancia Spiner
        languageSpinner = findViewById(R.id.languageSpinner)
        setupLanguageSpinner()

        // Obtengo el token para usarlo luego
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userToken = task.result
                Log.d("FCM Token", "Token obtenido manualmente: $userToken")
            } else {
                Log.e("FCM Token", "Error al obtener el token", task.exception)
            }
        }

    }

    override fun onStart(){
        super.onStart()

        var authLayout: ConstraintLayout = findViewById(R.id.constraintLayout);
        authLayout.visibility = View.VISIBLE;
    }

    private fun session(){
        val prefs: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id: String? = prefs.getString("id", null)
        val email: String? = prefs.getString("email", null)
        val provider: String? = prefs.getString("provider", null)
        val name: String? = prefs.getString("name", null)
        val role: String? = prefs.getString("role", null)

        if(id != null && email != null && provider != null && name != null && role != null){
            var authLayout: ConstraintLayout = findViewById(R.id.constraintLayout);
            authLayout.visibility = View.INVISIBLE;
            showHome(id, email, ProviderType.valueOf(provider), name, role)
        }
    }

    private fun setup(){
        title = "Autenticación"
        var signUpButton: Button = findViewById(R.id.signUpButton);
        var loginButton: Button = findViewById(R.id.logInButton);
        var googleButton: Button = findViewById(R.id.googleButton);
        var nameInput: EditText = findViewById(R.id.nameInput)
        var lastnameInput: EditText = findViewById(R.id.lastnameInput)
        var emailEditText: EditText = findViewById(R.id.emailInput);
        var passwordEditText: EditText = findViewById(R.id.passwordInput);
        val rePasswordInput: EditText = findViewById(R.id.rePasswordInput)

        val currentLanguage = getCurrentLanguage()
        val errorMessages = getErrorMessages(currentLanguage)

        signUpButton.setOnClickListener {

            // Limpiar errores anteriores
            nameInput.error = null
            lastnameInput.error = null
            emailEditText.error = null
            passwordEditText.error = null
            rePasswordInput.error = null

            // Validación: verificar si los campos están vacíos
            if (nameInput.text.isEmpty()) {
                nameInput.error = errorMessages["nameRequired"]
                return@setOnClickListener
            }

            if (lastnameInput.text.isEmpty()) {
                lastnameInput.error = errorMessages["lastnameRequired"]
                return@setOnClickListener
            }

            if (emailEditText.text.isEmpty()) {
                emailEditText.error = errorMessages["emailRequired"]
                return@setOnClickListener
            }

            // Validación: verificar el formato del correo
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()) {
                emailEditText.error = errorMessages["emailInvalid"]
                return@setOnClickListener
            }

            if (passwordEditText.text.isEmpty()) {
                passwordEditText.error = errorMessages["passwordRequired"]
                return@setOnClickListener
            }

            if (passwordEditText.text.length < 6) {
                passwordEditText.error = errorMessages["passwordTooShort"]
                return@setOnClickListener
            }

            if (passwordEditText.text.toString() != rePasswordInput.text.toString()) {
                rePasswordInput.error = errorMessages["passwordsDoNotMatch"]
                return@setOnClickListener
            }

            val radioGroup: RadioGroup = findViewById(R.id.roleRadioGroup)
            selectedRoleButton = radioGroup.checkedRadioButtonId
            if (selectedRoleButton != -1) {  // Verifica que haya un RadioButton seleccionado
                val selectedRadioButton: RadioButton = findViewById(selectedRoleButton)

                selectedRole = selectedRadioButton.text.toString()
                println("Seleccionado: $selectedRole")
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
                .addOnCompleteListener() {
                    if (it.isSuccessful) {
                        val uid = it.result?.user?.uid ?: ""
                        db.collection("users").document(uid).set(
                            hashMapOf(
                                "id" to uid,
                                "name" to nameInput.text.toString(),
                                "lastname" to lastnameInput.text.toString(),
                                "email" to emailEditText.text.toString(),
                                "password" to passwordEditText.text.toString(),
                                "token" to userToken,
                                "role" to selectedRole
                            )
                        )
                        showHome(uid, it.result.user?.email.toString() ?: "", ProviderType.BASIC, nameInput.text.toString() + lastnameInput.text.toString(), selectedRole)
                    } else {
                        showAlert()
                    }
                }
        }

        loginButton.setOnClickListener {

            emailEditText.error = null
            passwordEditText.error = null

            if (emailEditText.text.isEmpty()) {
                emailEditText.error = errorMessages["emailRequired"]
                return@setOnClickListener
            }

            // Validación: verificar el formato del correo
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()) {
                emailEditText.error = errorMessages["emailInvalid"]
                return@setOnClickListener
            }

            if (passwordEditText.text.isEmpty()) {
                passwordEditText.error = errorMessages["passwordRequired"]
                return@setOnClickListener
            }

            if (passwordEditText.text.length < 6) {
                passwordEditText.error = errorMessages["passwordTooShort"]
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
                .addOnCompleteListener() {
                    if (it.isSuccessful) {
                        lifecycleScope.launch {
                            val user = obtenerUsuarioPorId(it.result.user?.uid.toString())
                            if (user != null) {
                                println("Usuario encontrado: $user")
                                showHome(
                                    it.result.user?.uid.toString(),
                                    user.email ?: "",
                                    ProviderType.GOOGLE,
                                    user.name ?: "",
                                    user.role ?: ""
                                )
                            } else {
                                println("Usuario no encontrado.")
                                showAlert()
                            }
                        }
                    } else {
                        showAlert()
                    }
                }
        }

        googleButton.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient: GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    private fun getErrorMessages(language: String): Map<String, String> {
        return if (language == "en") {
            mapOf(
                "nameRequired" to "Name is required",
                "lastnameRequired" to "Last name is required",
                "emailRequired" to "Email is required",
                "emailInvalid" to "Invalid email format",
                "passwordRequired" to "Password is required",
                "passwordTooShort" to "Password must be at least 6 characters long",
                "passwordsDoNotMatch" to "Passwords do not match"
            )
        } else {
            mapOf(
                "nameRequired" to "El nombre es obligatorio",
                "lastnameRequired" to "El apellido es obligatorio",
                "emailRequired" to "El correo es obligatorio",
                "emailInvalid" to "Formato de correo no válido",
                "passwordRequired" to "La contraseña es obligatoria",
                "passwordTooShort" to "La contraseña debe tener al menos 6 caracteres",
                "passwordsDoNotMatch" to "Las contraseñas no coinciden"
            )
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(id: String, email: String, provider: ProviderType, name: String, role: String){
        val homeIntent = Intent(this, HomeActivity::class.java).apply{
            putExtra("id", id)
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("name", name)
            putExtra("role", role)
        }

        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener() {
                            if (it.isSuccessful) {
                                lifecycleScope.launch {
                                    val user = obtenerUsuarioPorId(account.id.toString())
                                    if (user != null) {
                                        println("Usuario encontrado: $user")
                                        showHome(
                                        account.id.toString(),
                                        account.email ?: "",
                                        ProviderType.GOOGLE,
                                        account.displayName ?: "",
                                        user.role ?: ""
                                    )
                                    } else {
                                        println("Usuario no encontrado.")
                                        showAlert()
                                    }
                                }
                            } else {
                                showAlert()
                            }
                        }
                }
            }
            catch(e: ApiException){
                showAlert()
            }

        }
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

    // Funcion para animar la expanción del cambio de tamaño del LinearLayout
    private fun expandBlock(view: View, targetPercentage: Float) {
        val parentLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        val heightInPx = dpToPx(180) // 200dp a px
        // Cambiar el tamaño del ImageView utilizando ConstraintSet
        constraintSet.constrainHeight(R.id.logoImage, heightInPx) // Nueva altura en píxeles

        val marginInPx = dpToPx(70) // Cambiar a tu valor deseado en dp
        // Cambiar el marginTop del ImageView
        constraintSet.setMargin(R.id.logoImage, ConstraintSet.TOP, marginInPx)

        // Aplicar los cambios del ConstraintSet
        constraintSet.applyTo(parentLayout)

        // Anima el cambio de altura del LinearLayout
        val initialHeight = 0.35f
        val startHeight = initialHeight
        val endHeight = targetPercentage
        val animator = ValueAnimator.ofFloat(startHeight, endHeight)
        animator.duration = 500 // duración de la Animation (in milliseconds)

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            constraintSet.constrainPercentHeight(R.id.expandableBlock, animatedValue)
            constraintSet.applyTo(parentLayout)
        }
        animator.start()
    }

    // Funcion para animar la reducción de altura del LinearLayout
    private fun collapseBlock(view: View, startHeight: Float, targetPercentage: Float) {
        val parentLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        val heightInPx = dpToPx(300)

        // Cambiar el tamaño del ImageView utilizando ConstraintSet
        constraintSet.constrainHeight(R.id.logoImage, heightInPx) // Nueva altura en píxeles

        // Aplicar los cambios del ConstraintSet
        constraintSet.applyTo(parentLayout)

        val animator = ValueAnimator.ofFloat(startHeight, targetPercentage)
        animator.duration = 500

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            constraintSet.constrainPercentHeight(R.id.expandableBlock, animatedValue)
            constraintSet.applyTo(parentLayout)
        }
        animator.start()
    }

    // Función para borrar los botones y agregar los inputs
    private fun removeButtonsAndShowForm(
        expandableBlock: LinearLayout, textDisplay: TextView, textLogo: TextView, registerButton: Button, loginButton: Button,
        name: EditText, lastName: EditText, email: EditText, password: EditText, rePassword: EditText, roleTextView: TextView,
        radioGroup: RadioGroup, confirmButton: Button, backButton: Button, googleButton: Button,
    ) {
        val currentLanguage = getCurrentLanguage()

        // Oculto los botones
        registerButton.visibility = View.GONE
        loginButton.visibility = View.GONE
        textLogo.visibility = View.INVISIBLE

        if (currentLanguage == "en") {
            // Lógica para inglés
            if(!flag){
                textDisplay.text = "Log in"
            } else {
                textDisplay.text = "Create an account"
            }
        } else if (currentLanguage == "es") {
            // Lógica para español
            if(!flag){
                textDisplay.text = "Iniciar sesión"
            } else {
                textDisplay.text = "Crear una cuenta"
            }
        }

        if(!flag){
            googleButton.visibility = View.VISIBLE
        } else {
            name.visibility = View.VISIBLE
            lastName.visibility = View.VISIBLE
            rePassword.visibility = View.VISIBLE
            roleTextView.visibility = View.VISIBLE
            radioGroup.visibility = View.VISIBLE
            googleButton.visibility = View.GONE
        }

        // Hago los elementos visibles
        textDisplay.visibility = View.VISIBLE
        email.visibility = View.VISIBLE
        password.visibility = View.VISIBLE
        confirmButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
    }

    // Funcion para restaurar los elementos previamente eliminados
    private fun restoreInitialState(
        expandableBlock: LinearLayout, textDisplay: TextView, textLogo: TextView, registerButton: Button, loginButton: Button,
        name: EditText, lastName: EditText, email: EditText, password: EditText, rePassword: EditText, roleTextView: TextView,
        radioGroup: RadioGroup, confirmButton: Button, backButton: Button, googleButton: Button
    ) {

        registerButton.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        textLogo.visibility = View.VISIBLE

        textDisplay.visibility = View.GONE
        name.visibility = View.GONE
        lastName.visibility = View.GONE
        email.visibility = View.GONE
        password.visibility = View.GONE
        rePassword.visibility = View.GONE
        roleTextView.visibility = View.GONE
        radioGroup.visibility = View.GONE
        confirmButton.visibility = View.GONE
        backButton.visibility = View.GONE
        googleButton.visibility = View.GONE
    }

    // Función para convertir dp a px
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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

    private fun getCurrentLanguage(): String {
        return prefs.getString("selectedLanguage", "es") ?: "es"
    }
}

class LanguageAdapter(context: Context, private val languages: List<Pair<Int, String>>) : ArrayAdapter<Pair<Int, String>>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_with_flag, parent, false)

        val flagImage = view.findViewById<ImageView>(R.id.flagImage)
        val languageText = view.findViewById<TextView>(R.id.languageText)

        val (imageRes, languageName) = languages[position]
        flagImage.setImageResource(imageRes)
        languageText.text = languageName

        return view
    }
}

public fun guardarTokenEnFirestore(userId: String, role: String, token: String) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    val userData = hashMapOf(
        "id" to userId,
        "role" to role,
        "fcmToken" to token
    )

    userRef.set(userData, SetOptions.merge())
        .addOnSuccessListener {
            println("Token FCM guardado correctamente en Firestore.")
        }
        .addOnFailureListener { exception ->
            println("Error al guardar el token FCM en Firestore: ${exception.message}")
        }
}



