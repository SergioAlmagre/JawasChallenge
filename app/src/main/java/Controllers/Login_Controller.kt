package Controllers

import Connections.FireStore
import Model.Users.User
import Tools.ProviderType
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityLoginBinding
import com.example.jawaschallenge.databinding.TestMainBinding
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
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.random.Random

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


        //------------------------------ Autenticación con email y password ------------------------------------
        binding.btnLoginMail.setOnClickListener {

            //------------------ Login Email -------------------
            if (binding.userMailInput.text!!.isNotEmpty() && binding.passwordInput.text!!.isNotEmpty()) {
                firebaseauth.signInWithEmailAndPassword(
                    binding.userMailInput.text.toString(),
                    binding.passwordInput.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        var usu: User? = null

                        runBlocking {
                            val trabajo: Job = launch(context = Dispatchers.Default) {
                                usu = FireStore.getUserByEmail(binding.userMailInput.text.toString())
                            }
                            trabajo.join()   // ESPERAMOS A QUE TERMINE EL TRABAJO ANTES DE SEGUIR
                        }
                        Log.d("usuarioLogin", usu.toString())
                        if (usu != null) {
                            irHome(
                                it.result?.user?.email ?: usu!!.email,
                                usu!!.role!!,
                                ProviderType.BASIC
                            )
                        }

                    } else {
                        showAlert("Usuario o contraseña incorrectos")
                    }
                }
            } else {
                showAlert("Rellene los campos")
            }
        }


        //------------------ Login Google -------------------
        //------------------------------- -Autenticación Google --------------------------------------------------
        firebaseauth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLoginGoogle.setOnClickListener {
            signInGoogle()
        }



        binding.btnCrearCuenta.setOnClickListener {

            // AÑADE EL USUARIO A FIREBASEUTH
            firebaseauth.createUserWithEmailAndPassword(
                binding.userMailInput.text.toString(),
                binding.passwordInput.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Usuario registrado",
                        Toast.LENGTH_SHORT
                    ).show()

                    // AÑADE EL USUARIO A FIRESTONE

                    Connections.FireStore.addUser(Factories.Factory.createUser("1"))


                    // IR A SIGUIENTE VENTANA MEDIANTA IRHOME
                    irHome(
                        it.result?.user?.email ?: "",
                        "1",
                        ProviderType.BASIC
                    )
                } else {
                    showAlert()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this, e.message, Toast.LENGTH_SHORT
                ).show()
                Log.e(
                    "FirebaseAuth",
                    "Error en la autenticación: " + e.message
                );
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
                val mail = account.email
                // Verifica si el usuario ya existe en la base de datos
                runBlocking {
//                    val usuarioExistente = Conexion.obtenerUsuario(account.email!!)
                    val existingUser = FireStore.getUserByEmail(account.email!!)
                    if (existingUser != null) {
                        // El usuario ya existe, va directamente a la página de inicio
                        irHome(mail ?: "", existingUser.role!!, ProviderType.BASIC)
                    } else {
                        updateUI(account)
                        // El usuario no existe, lo registra en la base de datos
                        var newUser = User("", mail!!,"","","defaultPictureUser.jpg","2")
                        FireStore.addUser(newUser )
//                        Conexion.registrarUsuario(mail ?: "", 0, "usuariodefault.jpg")
                        irHome(mail ?: "", newUser.role!!, ProviderType.BASIC)
                    }
                }
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseauth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                irHome(it.result?.user?.email ?: "", "2", ProviderType.BASIC)
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInGoogle() {
        val signInClient = googleSignInClient.signInIntent
        Log.e(TAG, "Llego aquí 1")
        launcherVentanaGoogle.launch(signInClient)
        //milauncherVentanaGoogle.launch(signInClient)
    }

    //************************************** Funciones auxiliares **************************************
    //*********************************************************************************
    private fun showAlert(msg: String = "Se ha producido un error autenticando al usuario") {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //*********************************************************************************
    private fun irHome(mail: String, rol: String, provider: ProviderType) {
        Log.e(TAG, "Valores:${mail}, ${provider}, ${rol}")
        if (rol == "0") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                putExtra("mail", mail)
                putExtra("rol", rol)
                putExtra("provider", provider)
            }
            startActivity(homeIntent)
        } else if (rol == "1") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                putExtra("mail", mail)
                putExtra("rol", rol)
                putExtra("provider", provider)
            }
            startActivity(homeIntent)
        }else if (rol == "2") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                putExtra("mail", mail)
                putExtra("rol", rol)
                putExtra("provider", provider)
            }
            startActivity(homeIntent)
        }else if (rol == "3") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                putExtra("mail", mail)
                putExtra("rol", rol)
                putExtra("provider", provider)
            }
            startActivity(homeIntent)
        }else if (rol == "4") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                putExtra("mail", mail)
                putExtra("rol", rol)
                putExtra("provider", provider)
            }
            startActivity(homeIntent)
        }

    }


}