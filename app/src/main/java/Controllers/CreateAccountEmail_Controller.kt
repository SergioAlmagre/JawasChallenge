package Controllers

import Connections.FireStore
import Model.Users.User
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.jawaschallenge.TestMain
import com.example.jawaschallenge.databinding.ActivityCreateAccountEmailBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CreateAccountEmail_Controller : AppCompatActivity() {
    lateinit var binding: ActivityCreateAccountEmailBinding
    private lateinit var firebaseauth: FirebaseAuth
    val TAG = "Sergio"
    var defaultRole = "2"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateAccountEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()

        binding.bntEnter.setOnClickListener {

            //*********************************MAIL************************************************
            if (binding.userMailInput.text!!.isNotEmpty() && binding.userPasswordInput.text!!.isNotEmpty()) {
                var name = binding.userNameInput.text.toString().uppercase().trim()
                var mail = binding.userMailInput.text.toString().uppercase().trim()
                var address = binding.userAddressInput.text.toString().uppercase().trim()
                var phone = binding.userPhoneInput.text.toString()
                var picture = "defaultPictureUser.jpg"

                var password = binding.userPasswordInput.text.toString()
                var rPassword = binding.userRepeatPasswordInput.text.toString()

                if(password != rPassword){
                    showAlert("The passwords are not the same")
                }else{
                    var newUser = User(name,mail,address,phone,picture,defaultRole)
                    createAccount(newUser, password)
                }
            } else {
                showAlert("Rellene los campos")
            }
        }
    }

    // Función para crear una cuenta
    private fun createAccount(user:User, password: String) {
        firebaseauth.createUserWithEmailAndPassword(user.email, password)
            .addOnCompleteListener { createTask ->
                if (createTask.isSuccessful) {
                    getUserAndNavigate(user.email)
                    // AÑADE EL USUARIO A FIRESTONE
                    FireStore.addUser(user)
//                    // IR A SIGUIENTE VENTANA MEDIANTA IRHOME
//                    goHome(user)
                } else {
                    showAlert("Error al crear la cuenta: ${createTask.exception?.message}")
                }
            }
    }


    // Función para obtener información del usuario y navegar a la pantalla de inicio
    private fun getUserAndNavigate(email: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val user = FireStore.getUserByEmail(email)
                Log.d("usuarioLogin", user.toString())

                withContext(Dispatchers.Main) {
                    user?.let {
                        goHome(user)
                    } ?: showAlert("No se pudo obtener la información del usuario")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showAlert("Error al obtener la información del usuario: ${e.message}")
                }
            }
        }
    }


    //************************************** Auxiliars Functions **************************************
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
    private fun goHome(user: User) {
        if (user.role == "0") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)

        } else if (user.role == "1") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }else if (user.role == "2") {
            val homeIntent = Intent(this, TestMain::class.java).apply {
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




//    //******************************* Para el login con Google ******************************
//    private lateinit var googleSignInClient: GoogleSignInClient
//
//    private val launcherVentanaGoogle =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            Log.e(TAG, "Llego aquí 2 ${result.data!!.extras.toString()}")
//            if (result.resultCode == Activity.RESULT_OK) {
//                Log.e(TAG, "Llego aquí 3")
//                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                handleResults(task)
//            }
//        }
//
//    private fun handleResults(task: Task<GoogleSignInAccount>) {
//        if (task.isSuccessful) {
//            val account: GoogleSignInAccount? = task.result
//            if (account != null) {
//                val mail = account.email
//                // Verifica si el usuario ya existe en la base de datos
//                runBlocking {
////                    val usuarioExistente = Conexion.obtenerUsuario(account.email!!)
//                    val existingUser = FireStore.getUserByEmail(account.email!!)
//                    if (existingUser != null) {
//                        // El usuario ya existe, va directamente a la página de inicio
//                        goHome(existingUser)
//                    } else {
//                        updateUI(account)
//                        // El usuario no existe, lo registra en la base de datos
//                        var name = binding.userNameInput.text.toString().uppercase().trim()
//                        var mail = binding.userMailInput.text.toString().uppercase().trim()
//                        var password = binding.userPasswordInput.text.toString()
//                        var address = binding.userAddressInput.text.toString().uppercase().trim()
//                        var phone = binding.userPhoneInput.text.toString()
//                        var picture = "defaultPictureUser.jpg"
//
//                        var newUser = User(name,mail,address,phone,picture,defaultRole)
//
//                        FireStore.addUser(newUser )
//                        goHome(newUser)
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateUI(account: GoogleSignInAccount) {
//        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//
//        firebaseauth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
//            if (signInTask.isSuccessful) {
//                val email = account.email
//                if (email != null) {
//                    GlobalScope.launch(Dispatchers.Main) {
//                        try {
//                            val existingUser = withContext(Dispatchers.IO) {
//                                FireStore.getUserByEmail(email)
//                            }
//
//                            if (existingUser != null) {
//                                goHome(existingUser)
//                            } else {
//                                // El usuario no existe en la base de datos
//                                // Puedes manejar este caso según tus necesidades
//                            }
//                        } catch (e: Exception) {
//                            Log.e( "Sergio", "Error al obtener el usuario: ${e.message}")
//                        }
//                    }
//                } else {
//                    // El correo electrónico es nulo, manejar este caso según tus necesidades
//                }
//            } else {
//                Log.e( "Sergio", signInTask.exception.toString())
//            }
//        }
//    }
//
//
//    private fun signInGoogle() {
//        val signInClient = googleSignInClient.signInIntent
//        Log.e(TAG, "Llego aquí 1")
//        launcherVentanaGoogle.launch(signInClient)
//        //milauncherVentanaGoogle.launch(signInClient)
//    }

}