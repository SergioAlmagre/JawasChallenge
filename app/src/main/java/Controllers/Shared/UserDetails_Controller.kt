package Controllers.Shared

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Accounts.Login_Controller
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
import com.example.jawaschallenge.databinding.ActivityUserDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File

class UserDetails_Controller : AppCompatActivity() {
    lateinit var binding: ActivityUserDetailsBinding
    private lateinit var firebaseauth: FirebaseAuth
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap
    val auth = FirebaseAuth.getInstance()

    val storage = Firebase.storage
    val storageRef = storage.reference
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        binding.txtPasswordRecovery.setOnClickListener {
            if(InterWindows.iwUser.email.isNotEmpty()){
                auth.sendPasswordResetEmail(InterWindows.iwUser.email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_SHORT).show()
                        } else {

                        }
                    }
            }
        }


        var roles = ArrayList<String>()
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                roles = FireStore.getAllRoles()

                if(!InterWindows.iwUser.email.isNullOrEmpty()){
                    if(InterWindows.iwUser!!.picture != InterWindows.iwUser!!.email){
                        Log.d("SergioFoto", "Foto: " + InterWindows.iwUser!!.picture)
                        fileDownload(InterWindows.iwUser!!.picture!!)
                    }else{
                        Log.d("SergioMail", "Foto: " + InterWindows.iwUser!!.email)
                        fileDownload(InterWindows.iwUser!!.email)
                    }
                }
            }

            job.join()
        }
        binding.txtNameUserAdmin.setText(InterWindows.iwUser.name)
        binding.txtEmailUserAdmin.setText(InterWindows.iwUser.email)
        binding.txtAddressUserAdmin.setText(InterWindows.iwUser.address)
        binding.txtPhoneUserAdmin.setText(InterWindows.iwUser.phone)

        binding.btnLogOutUserAdmin.setOnClickListener {
           signOutAndRedirectToLogin()
        }

        binding.btnHomeAdmin.setOnClickListener {
            Log.d("userAdmin", InterWindows.iwUser.toString())

            if(isDifferent()){
                with(builder)
                {
                    setTitle("Cambios en perfil detectados")
                    setMessage("Desea guardar los cambios?")
                    setPositiveButton(
                        "Si",
                        android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                            buildUser()
                            runBlocking {
                                val job : Job = launch(context = Dispatchers.Default) {
                                    FireStore.updateAllDataUser(user!!)
                                    uploadPictureOK()
                                    InterWindows.iwUser = user!!
                                }
                                job.join()

                            }
                            Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                            finish()
                        })
                    )
                    setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->

                        finish()

                    }))
                    show()
                }

            }else{
                finish()
            }
        }

        binding.btnSaveChangesUserAdmin.setOnClickListener {
            buildUser()
            runBlocking {
                val job : Job = launch(context = Dispatchers.Default) {
                    FireStore.updateAllDataUser(user!!)
                    uploadPictureOK()
                    InterWindows.iwUser = user!!
                }
                job.join()

            }
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnAddPhotoEm2.setOnClickListener {
            with(builder)
            {
                setTitle("Cual es el origen de la imagen")
                setMessage("Desea hacerla con la cámara o con la galería?")
                setPositiveButton(
                    "Cámara",
                    android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->

                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    })
                )
                setNegativeButton("Galería", ({ dialog: DialogInterface, which: Int ->

                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }))
                show()
            }
        }


    }// End of onCreate


    fun buildUser(){
        var name = binding.txtNameUserAdmin.text.toString().uppercase().trim()
        var email = binding.txtEmailUserAdmin.text.toString().uppercase().trim()
        var address = binding.txtAddressUserAdmin.text.toString().uppercase().trim()
        var phone = binding.txtPhoneUserAdmin.text.toString().uppercase().trim()
        var picture = InterWindows.iwUser.picture
        var role = InterWindows.iwUser.role

            user = User(
            name,
            email,
            address,
            phone,
            picture,
            role!!
        )
        user!!.batches = InterWindows.iwUser.batches
        InterWindows.iwUser = user!!
    }

    fun isDifferent(): Boolean{
        var isDifferent = false
        if(InterWindows.iwUser.name != binding.txtNameUserAdmin.text.toString().uppercase().trim()){
            isDifferent = true
        }
        if(InterWindows.iwUser.email != binding.txtEmailUserAdmin.text.toString().uppercase().trim()){
            isDifferent = true
        }
        if(InterWindows.iwUser.address != binding.txtAddressUserAdmin.text.toString().uppercase().trim()){
            isDifferent = true
        }
        if(InterWindows.iwUser.phone != binding.txtPhoneUserAdmin.text.toString().uppercase().trim()){
            isDifferent = true
        }
        if(InterWindows.iwUser.picture != InterWindows.iwUser.picture){
            isDifferent = true
        }
        return isDifferent
    }


    fun signOutAndRedirectToLogin() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Login_Controller::class.java)
        startActivity(intent)
        finish()
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
                    binding.pictureUserAdmin.setImageBitmap(imageBitmap)

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
            binding.pictureUserAdmin.setImageURI(uri)// Coloca la imagen en el pictureBox
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
            binding.pictureUserAdmin.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this, "Algo ha fallado en la descarga", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadPictureOK(){
        binding.pictureUserAdmin.isDrawingCacheEnabled = true
        binding.pictureUserAdmin.buildDrawingCache()
        val bitmap = (binding.pictureUserAdmin.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data2 = baos.toByteArray()

        val imagesRef = storageRef.child("UsersPictures/")
        var pictureName = binding.txtEmailUserAdmin.text.toString().uppercase().trim()

        InterWindows.iwUser.picture = InterWindows.iwUser.email

        val uploadTask = imagesRef.child(pictureName).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{
            //Toast.makeText(this@Registro, "Error en la subida de la imagen", Toast.LENGTH_SHORT).show()
        }
    }


} // End of class UserDetailsAdmin_Controller
