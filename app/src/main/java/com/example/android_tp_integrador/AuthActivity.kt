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
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val GOOGLE_SIGN_IN = 100
    private val db = FirebaseFirestore.getInstance();
    var flag: Boolean = true
    lateinit var selectedRole: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val radioGroup: RadioGroup = findViewById(R.id.roleRadioGroup)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val logInButton: Button = findViewById(R.id.logInButton)
        val backButton: Button = findViewById(R.id.backButton)
        val textDisplay: TextView = findViewById(R.id.textDisplay)
        var googleButton: Button = findViewById(R.id.googleButton);

        // Obtengo el rol seleccionado
        selectedRole = when (radioGroup.checkedRadioButtonId) {
            R.id.denuncianteRadioButton -> "Denunciante"
            R.id.protectorRadioButton -> "Protector"
            else -> ""
        }

        // Eventos para los botones
        registerButton.setOnClickListener {
            flag = true
            expandBlock(expandableBlock, 0.72f)
            removeButtonsAndShowForm(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, signUpButton, backButton, googleButton)
        }

        loginButton.setOnClickListener {
            flag = false
            expandBlock(expandableBlock, 0.55f)
            removeButtonsAndShowForm(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, logInButton, backButton, googleButton)
        }

        // Evento para el botón volver
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

            if(!flag){ //login
                collapseBlock(expandableBlock, 0.55f, 0.35f)
                restoreInitialState(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, logInButton, backButton, googleButton)
            } else { //register
                collapseBlock(expandableBlock, 0.72f, 0.35f)
                restoreInitialState(expandableBlock, textDisplay, textLogo, registerButton, loginButton, nameInput, lastnameInput, emailInput, passwordInput, rePasswordInput, roleTextView, radioGroup, signUpButton, backButton, googleButton)
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
        val email: String? = prefs.getString("email", null)
        val provider: String? = prefs.getString("provider", null)
        val name: String? = prefs.getString("name", null)

        if(email != null && provider != null && name != null){
            var authLayout: ConstraintLayout = findViewById(R.id.constraintLayout);
            authLayout.visibility = View.INVISIBLE;
            showHome(email, ProviderType.valueOf(provider), name)
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

        signUpButton.setOnClickListener {

            // Limpiar errores anteriores
            nameInput.error = null
            lastnameInput.error = null
            emailEditText.error = null
            passwordEditText.error = null
            rePasswordInput.error = null

            // Validación: verificar si los campos están vacíos
            if (nameInput.text.isEmpty()) {
                nameInput.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            if (lastnameInput.text.isEmpty()) {
                lastnameInput.error = "El apellido es obligatorio"
                return@setOnClickListener
            }

            if (emailEditText.text.isEmpty()) {
                emailEditText.error = "El correo es obligatorio"
                return@setOnClickListener
            }

            // Validación: verificar el formato del correo
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()) {
                emailEditText.error = "Formato de correo no válido"
                return@setOnClickListener
            }

            // Validación: verificar si la contraseña es obligatoria
            if (passwordEditText.text.isEmpty()) {
                passwordEditText.error = "La contraseña es obligatoria"
                return@setOnClickListener
            }

            // Validación: verificar longitud de la contraseña
            if (passwordEditText.text.length < 6) {
                passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            // Validación: verificar si las contraseñas coinciden
            if (passwordEditText.text.toString() != rePasswordInput.text.toString()) {
                rePasswordInput.error = "Las contraseñas no coinciden"
                return@setOnClickListener
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
                                "role" to selectedRole
                            )
                        )
                        showHome(it.result.user?.email.toString() ?: "", ProviderType.BASIC, it.result.user?.displayName ?: "")
                    } else {
                        showAlert()
                    }
                }
        }

        loginButton.setOnClickListener {

            emailEditText.error = null
            passwordEditText.error = null

            if (emailEditText.text.isEmpty()) {
                emailEditText.error = "El correo es obligatorio"
                return@setOnClickListener
            }

            // Validación: verificar el formato del correo
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()) {
                emailEditText.error = "Formato de correo no válido"
                return@setOnClickListener
            }

            // Validación: verificar si la contraseña es obligatoria
            if (passwordEditText.text.isEmpty()) {
                passwordEditText.error = "La contraseña es obligatoria"
                return@setOnClickListener
            }

            // Validación: verificar longitud de la contraseña
            if (passwordEditText.text.length < 6) {
                passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
                .addOnCompleteListener() {
                    if (it.isSuccessful) {
                        showHome(it.result.user?.email.toString() ?: "", ProviderType.BASIC, it.result.user?.displayName ?: "")
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

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun validationAlert(value: Int){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        if(value == 1){
            builder.setMessage("El campo email es obligatorio")
        } else {
            builder.setMessage("El campo contraseña es obligatorio")
        }
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType, name: String){
        val homeIntent = Intent(this, HomeActivity::class.java).apply{
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("name", name)
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
                                showHome(account.email ?: "", ProviderType.GOOGLE, account.displayName ?: "")
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

    // Funcion para animar la expanción del cambio de tamaño del LinearLayout
    private fun expandBlock(view: View, targetPercentage: Float) {
        val parentLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        val heightInPx = dpToPx(195) // 200dp a px
        // Cambiar el tamaño del ImageView utilizando ConstraintSet
        constraintSet.constrainHeight(R.id.logoImage, heightInPx) // Nueva altura en píxeles

        val marginInPx = dpToPx(10) // Cambiar a tu valor deseado en dp
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
        expandableBlock: LinearLayout,
        textDisplay: TextView,
        textLogo: TextView,
        registerButton: Button,
        loginButton: Button,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: EditText,
        rePassword: EditText,
        roleTextView: TextView,
        radioGroup: RadioGroup,
        confirmButton: Button,
        backButton: Button,
        googleButton: Button,
    ) {
        // Oculto los botones
        registerButton.visibility = View.GONE
        loginButton.visibility = View.GONE
        textLogo.visibility = View.INVISIBLE

        if(!flag){
            textDisplay.text = "Iniciar sesión"
            googleButton.visibility = View.VISIBLE
        } else {
            textDisplay.text = "Crear una cuenta"
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
        expandableBlock: LinearLayout,
        textDisplay: TextView,
        textLogo: TextView,
        registerButton: Button,
        loginButton: Button,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: EditText,
        rePassword: EditText,
        roleTextView: TextView,
        radioGroup: RadioGroup,
        confirmButton: Button,
        backButton: Button,
        googleButton: Button
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

}