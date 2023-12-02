package Controllers

import Connections.FireStore
import Constants.Routes
import Model.Users.User
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityLoginBinding
import com.example.jawaschallenge.TestMain
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login_Controller : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    //Para la autenticación, de cualquier tipo.
    private lateinit var firebaseauth: FirebaseAuth
    val db = Firebase.firestore
    val TAG = "Sergio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_formulario)
        val builder = AlertDialog.Builder(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Para la autenticación, de cualquier tipo.
        firebaseauth = FirebaseAuth.getInstance()


        //------------------ Sign In Email -------------------
        // Evento clic para el botón de inicio de sesión
        binding.btnSingInMail.setOnClickListener {
            if (binding.userMailInput.text!!.isNotEmpty() && binding.userPasswordInput.text!!.isNotEmpty()) {
                var email = binding.userMailInput.text.toString().uppercase().trim()
                var password = binding.userPasswordInput.text.toString()
                signIn(email,password)
            } else {
                showAlert("Rellene los campos")
            }
        }

        //------------------ Sign In Google -------------------
        binding.btnSignInGoogle.setOnClickListener {
            signInGoogle()
        }


        //------------------------------- -Autenticación Google --------------------------------------------------
        firebaseauth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)



        binding.btnNewAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Registro")
                .setMessage("¿Cómo desea registrarse?")
                .setPositiveButton("Correo Electrónico") { _, _ ->
                    // Acción cuando elige registrarse con correo electrónico
                    goNewAccountEmail()
                }
                .setNegativeButton("Google") { _, _ ->
                    // Acción cuando elige registrarse con Google
                    // Implementa aquí la lógica para el registro con Google
                    // Puedes utilizar Firebase Auth con Google SignIn, por ejemplo

                    signInGoogle()

                }
                .setNeutralButton("Cancelar") { dialog, _ ->
                    // Acción cuando elige cancelar
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnInfoApp.setOnClickListener {
            goWelcome()
        }


    }


    // Función para iniciar sesión con mail
    private fun signIn(email: String, password: String) {
        firebaseauth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    getUserAndNavigate(email)
                } else {
                    showAlert("Usuario o contraseña incorrectos")
                }
            }
    }

    private fun getUserAndNavigate(email: String) {
        lifecycleScope.launch {
            try {
                var us: User? = null
                val job = withContext(Dispatchers.Default) {
                    us = FireStore.getUserByEmail(email)
                }
                withContext(Dispatchers.Main) {
                    // Actualizar vistas de la interfaz de usuario aquí
                        goHome(us!!)
                        Log.d("signIn", us.toString())
                    }

            } catch (e: Exception) {
                showAlert("Error al obtener la información del usuario: ${e.message}")
            }
        }
    }


    //******************************* Para el login con Google ******************************
    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcherVentanaGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, "Llego aquí 2 ${result.data!!.extras.toString()}")
            if (result.resultCode == Activity.RESULT_OK) {
                Log.e(TAG, "Llego aquí 3")
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                val mail = account.email.toString().uppercase().trim()
                Log.d("mail", mail)
                // Verifica si el usuario ya existe en la base de datos
                lifecycleScope.launch {
                    try {
                        var existingUser: User? = null
                        withContext(Dispatchers.Default) {
                            existingUser = FireStore.getUserByEmail(mail!!)
                            Log.d("existingUser", existingUser.toString())
                        }

                        withContext(Dispatchers.Main) {
                            // Actualizar vistas de la interfaz de usuario aquí
                            if (existingUser != null) {
                                // El usuario ya existe, va directamente a la página de inicio
                                goHome(existingUser!!)
                            } else {
                                updateUI(account)
                                // El usuario no existe, se redirige a la página de nueva cuenta
                                Auxiliaries.InterWindows.iwUser = User("", mail!!,"","",Routes.defaultUserPictureName,"2")
                                goNewAccountGoogle()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Count", "Error: $e")
                    }
                }
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseauth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful) {
                val email = account.email
                if (email != null) {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            val existingUser = withContext(Dispatchers.IO) {
                                FireStore.getUserByEmail(email)
                            }

                            if (existingUser != null) {
                                goHome(existingUser)
                            } else {
                                // El usuario no existe en la base de datos, lo registra
                                var newUser = User("", email,"","",Routes.defaultUserPictureName,"2")
                                FireStore.addUser(newUser)
                                Auxiliaries.InterWindows.iwUser = newUser
                            }
                        } catch (e: Exception) {
                            Log.e( "Sergio", "Error al obtener el usuario: ${e.message}")
                        }
                    }
                } else {
                    // El correo electrónico es nulo, manejar este caso según tus necesidades
                }
            } else {
                Log.e( "Sergio", signInTask.exception.toString())
            }
        }
    }

    private fun signInGoogle() {
        val signInClient = googleSignInClient.signInIntent
        Log.e(TAG, "Llego aquí 1")
        launcherVentanaGoogle.launch(signInClient)
        //milauncherVentanaGoogle.launch(signInClient)
    }

    //************************************** Auxiliaries Functions **************************************
    private fun showAlert(msg: String = "Se ha producido un error autenticando al usuario") {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //*********************************************************************************
    private fun goHome(user: User) {
        if (user.role == "0") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)

        } else if (user.role == "1") {
            val homeIntent = Intent(this, AdministratorElection_Controller::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }else if (user.role == "2") {
            val homeIntent = Intent(this, JewelsCrud_Controller::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }else if (user.role == "3") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }

    }

    private fun goNewAccountEmail() {
        val homeIntent =  Intent(this, CreateAccountEmail_Controller::class.java)
        startActivity(homeIntent)
    }

    private fun goNewAccountGoogle() {
        val homeIntent =  Intent(this, CreateAccountGoogle_Controller::class.java)
        startActivity(homeIntent)
    }

    private fun goWelcome() {
        val homeIntent =  Intent(this, Welcome_Controller::class.java)
        startActivity(homeIntent)
    }



}