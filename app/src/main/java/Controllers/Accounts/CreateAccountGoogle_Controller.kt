package Controllers.Accounts

import Auxiliaries.InterWindows
import Connections.FireStore
import Model.Users.User
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jawaschallenge.TestMain
import com.example.jawaschallenge.databinding.ActivityCreateAccountGoogleBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class CreateAccountGoogle_Controller : AppCompatActivity() {
    lateinit var binding: ActivityCreateAccountGoogleBinding
    private lateinit var firebaseauth: FirebaseAuth
    val TAG = "Sergio"
    var user = InterWindows.iwUser
    var defaultRole = "2"
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap

    val storage = Firebase.storage
    val storageRef = storage.reference
    val filePath = "UsersPictures/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateAccountGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)


        binding.bntEnter.setOnClickListener {
            //*********************************GOOGLE************************************************
            var name = binding.userNameInput.text.toString().uppercase().trim()
            var phone = binding.userPhoneInput.text.toString()
            var address = binding.userAddressInput.text.toString().uppercase().trim()

            var newUser = User(name,user.email,address,phone,user.picture,defaultRole)

            updateUserDataCreateAccount(newUser)
            uploadPictureOK()

            InterWindows.iwUser = newUser
            goHome(Auxiliaries.InterWindows.iwUser)
        }




        binding.btnAddPhotoGo.setOnClickListener {
            with(builder)
            {
                setTitle("Cual es el origen de la imagen")
                setMessage("Desea hacerla con la cámara o con la galería?")
                setPositiveButton(
                    "Cámara",
                    android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->

                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//                    saveImageToGallery(intent.getStringExtra("mail").toString() + "jpg")

                    })
                )
                setNegativeButton("Galería", ({ dialog: DialogInterface, which: Int ->

                    pickMedia.launch(PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))

                }))
                show()
            }
        }


    }// End of onCreate

    //__________________________CAMERA_______________________________
    //Segunda activity para lanzar la cámara.
    val openCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    // La imagen capturada está en el objeto `imageBitmap`
                    this.bitmap = imageBitmap
                    binding.userPicture.setImageBitmap(imageBitmap)

                    Log.d("ComprobacionFuera", InterWindows.iwUser.email)

                    Glide.with(this)
                        .asBitmap()
                        .load(result)
                        .apply(
                            RequestOptions().override(500, 500).centerCrop()
                        ) // Cambia las dimensiones según tus necesidades // Con el centerCrop la recortamos cuadrada
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                // Convierte el Bitmap en un ByteArray
                                val outputStream = ByteArrayOutputStream()
                                resource.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    80,
                                    outputStream
                                ) // Cambia la calidad según tus necesidades

                                val byteArray = outputStream.toByteArray()
                                Log.d("ComprobacionDentro", InterWindows.iwUser.toString())
                                // Sube el ByteArray a Firebase Storage
                                val Folder: StorageReference =
                                    FirebaseStorage.getInstance().reference.child("UsersPictures/")
                                val file_name: StorageReference = Folder.child(InterWindows.iwUser!!.email)
                                Log.d("actualizarDocumentoFotoCamara2", InterWindows.iwUser.toString())
                                file_name.putBytes(byteArray)
                                    .addOnSuccessListener { taskSnapshot ->
                                        file_name.downloadUrl.addOnSuccessListener { uri ->
                                            // La imagen se subió correctamente y puedes obtener la URL de descarga
                                            InterWindows.iwUser.picture = InterWindows.iwUser.email

                                            Log.d("actualizarDocumentoFotoCamara3", InterWindows.iwUser.toString()
                                            )
                                        }
                                    }
                            }
                        })

                } else {
                    Log.d("Sergio", "No media selected")
                }
            }
        }


    val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                openCamera.launch(intent)
            } else {
                Log.e("Sergio", "Permiso de cámara no concedido")
            }
        }


    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.userPicture.setImageURI(uri)// Coloca la imagen en el pictureBox
            // Redimensiona y comprime la imagen con Glide antes de subirla
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .apply(RequestOptions().override(500, 500).centerCrop()) // Cambia las dimensiones según tus necesidades // Con el centerCrop la recortamos cuadrada
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Convierte el Bitmap en un ByteArray
                        val outputStream = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Cambia la calidad según tus necesidades
                        val byteArray = outputStream.toByteArray()

                        // Sube el ByteArray a Firebase Storage
                        val Folder: StorageReference = FirebaseStorage.getInstance().getReference().child("UsersPictures/")
                        val file_name: StorageReference = Folder.child(InterWindows.iwUser!!.email)
                        file_name.putBytes(byteArray)
                            .addOnSuccessListener { taskSnapshot ->
                                file_name.downloadUrl.addOnSuccessListener { uri ->
                                    // La imagen se subió correctamente y puedes obtener la URL de descarga
//                                    Conexion.actualizarDocumento(u!!.mail, u!!.mail)
                                    InterWindows.iwUser.picture = InterWindows.iwUser.email

                                }
                            }
                    }
                })
        } else {
            Log.d("Sergio", "No media selected")
        }
    }



    fun fileDownload(identificador: String) {

        var spaceRef = storageRef.child(filePath + identificador)
        val localfile = File.createTempFile(identificador, "jpg")
        spaceRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.userPicture.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this, "Algo ha fallado en la descarga", Toast.LENGTH_SHORT).show()
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
                        goHome(existingUser)
                    } else {
                        updateUI(account)
                        // El usuario no existe, lo registra en la base de datos
                        var name = binding.userNameInput.text.toString().uppercase().trim()
                        var mail = user.email.uppercase().trim()
                        var address = binding.userAddressInput.text.toString().uppercase().trim()
                        var phone = binding.userPhoneInput.text.toString()
                        var picture = "userdefaultpicture.jpg"

                        var newUser = User(name,mail,address,phone,picture,"1")

                        FireStore.addUser(newUser)
                        goHome(newUser)
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
                                // El usuario no existe en la base de datos
                                // Puedes manejar este caso según tus necesidades
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
    }

    private fun updateUserDataCreateAccount(user: User) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                FireStore.updateAllDataUser(user)

                Log.d("usuarioLogin", user.toString())

                withContext(Dispatchers.Main) {

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showAlert("Error al actualizar la información del usuario: ${e.message}")
                }
            }
        }
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

    fun uploadPictureOK(){
        binding.userPicture.isDrawingCacheEnabled = true
        binding.userPicture.buildDrawingCache()
        val bitmap = (binding.userPicture.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data2 = baos.toByteArray()

        val imagesRef = storageRef.child("UsersPictures/")
        var pictureName = InterWindows.iwUser!!.email.uppercase().trim()

        InterWindows.iwUser.picture = InterWindows.iwUser.email

        val uploadTask = imagesRef.child(pictureName).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{
            //Toast.makeText(this@Registro, "Error en la subida de la imagen", Toast.LENGTH_SHORT).show()
        }
    }



} // End of class