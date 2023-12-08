package Controllers.Accounts

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Administrator.AdministratorElection_Controller
import Controllers.Classifier.ClassifierCrud_Controller
import Controllers.Donor.DonorCrud_Controller
import Controllers.Jeweler.JewelsCrud_Controller
import Model.Users.User
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
import com.example.jawaschallenge.databinding.ActivityCreateAccountEmailBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class CreateAccountEmail_Controller : AppCompatActivity() {
    lateinit var binding: ActivityCreateAccountEmailBinding
    private lateinit var firebaseauth: FirebaseAuth
    private lateinit var bitmap: Bitmap

    val storage = Firebase.storage
    val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InterWindows.iwUser.picture = Routes.defaultUserPictureName

        binding = ActivityCreateAccountEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        binding.bntEnter.setOnClickListener {

            //*********************************MAIL************************************************
            if (binding.userMailInput.text!!.isNotEmpty() && binding.userPasswordInput.text!!.isNotEmpty()) {
                var name = binding.userNameInput.text.toString().uppercase().trim()
                var mail = binding.userMailInput.text.toString().uppercase().trim()
                var address = binding.userAddressInput.text.toString().uppercase().trim()
                var phone = binding.userPhoneInput.text.toString()
                var password = binding.userPasswordInput.text.toString()
                var rPassword = binding.userRepeatPasswordInput.text.toString()


                if(password != rPassword){
                    showAlert("The passwords are not the same")
                } else {
                    if(name.isEmpty()){
                        name = mail.substringBefore("@").uppercase().trim()
                    }

                    InterWindows.iwUser = User(name, mail, address, phone, InterWindows.iwUser.picture, Routes.defaultRole)
                    if(InterWindows.iwUser.picture != Routes.defaultUserPictureName){
                        uploadPictureOK()
                    }
                    createAccount(InterWindows.iwUser, password)
                }

            } else {
                showAlert("Rellene los campos")
            }
        }

        binding.btnAddPhotoEm.setOnClickListener{
            if(binding.userMailInput.text!!.isNotEmpty()){
                InterWindows.iwUser.email = binding.userMailInput.text.toString().uppercase().trim()
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

                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                    }))
                    show()
                }
            }else{
                showAlert("First, you have to enter the email")
            }
        }


    } // End of onCreate


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
    private fun showAlert(msg: String = "Se ha producido un error autenticando al usuario") {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


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
            val homeIntent = Intent(this, DonorCrud_Controller::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }else if (user.role == "3") {
            val homeIntent = Intent(this, ClassifierCrud_Controller::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }else if (user.role == "4") {
            val homeIntent = Intent(this, JewelsCrud_Controller::class.java).apply {
                Auxiliaries.InterWindows.iwUser = user
            }
            startActivity(homeIntent)
        }

    }



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
                                    FirebaseStorage.getInstance().reference.child(Routes.usersPicturesPath)
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
                        val Folder: StorageReference = FirebaseStorage.getInstance().getReference().child(Routes.usersPicturesPath)
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

        var spaceRef = storageRef.child(Routes.usersPicturesPath + identificador)
        val localfile = File.createTempFile(identificador, "jpg")
        spaceRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.userPicture.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this, "Algo ha fallado en la descarga", Toast.LENGTH_SHORT).show()
        }
    }


    fun uploadPictureOK(){
        binding.userPicture.isDrawingCacheEnabled = true
        binding.userPicture.buildDrawingCache()
        val bitmap = (binding.userPicture.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data2 = baos.toByteArray()

        val imagesRef = storageRef.child(Routes.usersPicturesPath)
        var pictureName = binding.userMailInput.text.toString().uppercase().trim()

        InterWindows.iwUser.picture = InterWindows.iwUser.email

        val uploadTask = imagesRef.child(pictureName).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{
            //Toast.makeText(this@Registro, "Error en la subida de la imagen", Toast.LENGTH_SHORT).show()
        }
    }


} // End of class