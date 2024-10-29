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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val GOOGLE_SIGN_IN = 100
    var flag: Boolean = true

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
        val loginButton: Button = findViewById(R.id.loginButton)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val logInButton: Button = findViewById(R.id.logInButton)
        val backButton: Button = findViewById(R.id.backButton)
        val textDisplay: TextView = findViewById(R.id.textDisplay)
        val anonymousTextBtn: TextView = findViewById(R.id.anonymousBtn)

        // Eventos para los botones
        registerButton.setOnClickListener {
            flag = true
            expandBlock(expandableBlock, 0.55f)
            removeButtonsAndShowForm(expandableBlock, textDisplay, registerButton, loginButton, emailInput, passwordInput, signUpButton, anonymousTextBtn, backButton)
        }

        loginButton.setOnClickListener {
            flag = false
            expandBlock(expandableBlock, 0.55f)
            removeButtonsAndShowForm(expandableBlock, textDisplay, registerButton, loginButton, emailInput, passwordInput, logInButton, anonymousTextBtn, backButton)
        }

        // Evento para el botón volver
        backButton.setOnClickListener {
            collapseBlock(expandableBlock, 0.35f)
            if(!flag){
                restoreInitialState(expandableBlock, textDisplay, registerButton, loginButton, emailInput, passwordInput, logInButton, anonymousTextBtn, backButton)
            } else {
                restoreInitialState(expandableBlock, textDisplay, registerButton, loginButton, emailInput, passwordInput, signUpButton, anonymousTextBtn, backButton)
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

        if(email != null && provider != null){
            var authLayout: ConstraintLayout = findViewById(R.id.constraintLayout);
            authLayout.visibility = View.INVISIBLE;
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup(){
        title = "Autenticación"
        var signUpButton: Button = findViewById(R.id.signUpButton);
        var loginButton: Button = findViewById(R.id.logInButton);
        //var googleButton: Button = findViewById(R.id.googleButton);
        var emailEditText: EditText = findViewById(R.id.emailInput);
        var passwordEditText: EditText = findViewById(R.id.passwordInput);

        signUpButton.setOnClickListener {
            if(emailEditText.text.isEmpty()){
                validationAlert(1)
            } else if(emailEditText.text.isNotEmpty() && passwordEditText.text.isEmpty()){
                validationAlert(0)
            }

            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
                    .addOnCompleteListener() {
                        if (it.isSuccessful) {
                            showHome(it.result.user?.email.toString() ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        loginButton.setOnClickListener {
            if(emailEditText.text.isEmpty()){
                validationAlert(1)
            } else if(emailEditText.text.isNotEmpty() && passwordEditText.text.isEmpty()){
                validationAlert(0)
            }

            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
                    .addOnCompleteListener() {
                        if (it.isSuccessful) {
                            showHome(it.result.user?.email.toString() ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                    }
            }
        }

//        googleButton.setOnClickListener {
//            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//
//            val googleClient: GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
//            googleClient.signOut()
//
//            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
//        }
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

    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java).apply{
            putExtra("email", email)
            putExtra("provider", provider.name)
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
                                showHome(account.email ?: "", ProviderType.GOOGLE)
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

        val heightInPx = dpToPx(200) // 200dp a px

        // Cambiar el tamaño del ImageView utilizando ConstraintSet
        constraintSet.constrainHeight(R.id.logoImage, heightInPx) // Nueva altura en píxeles

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
    private fun collapseBlock(view: View, targetPercentage: Float) {
        val parentLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        val heightInPx = dpToPx(300)

        // Cambiar el tamaño del ImageView utilizando ConstraintSet
        constraintSet.constrainHeight(R.id.logoImage, heightInPx) // Nueva altura en píxeles

        // Aplicar los cambios del ConstraintSet
        constraintSet.applyTo(parentLayout)

        val animator = ValueAnimator.ofFloat(0.55f, targetPercentage)
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
        registerButton: Button,
        loginButton: Button,
        email: EditText,
        password: EditText,
        confirmButton: Button,
        anonymousBtn: TextView,
        backButton: Button
    ) {
        // Oculto los botones
        registerButton.visibility = View.GONE
        loginButton.visibility = View.GONE
        anonymousBtn.visibility = View.GONE

        if(!flag){
            textDisplay.text = "Iniciar sesión"
        } else {
            textDisplay.text = "Crear una cuenta"
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
        registerButton: Button,
        loginButton: Button,
        email: EditText,
        password: EditText,
        confirmButton: Button,
        anonymousBtn: TextView,
        backButton: Button
    ) {

        registerButton.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        anonymousBtn.visibility = View.VISIBLE

        textDisplay.visibility = View.GONE
        email.visibility = View.GONE
        password.visibility = View.GONE
        confirmButton.visibility = View.GONE
        backButton.visibility = View.GONE
    }

    // Función para convertir dp a px
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

}