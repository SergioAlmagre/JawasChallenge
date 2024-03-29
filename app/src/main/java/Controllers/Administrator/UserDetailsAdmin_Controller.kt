package Controllers.Administrator

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityUserDetailsAdminBinding
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

class UserDetailsAdmin_Controller : AppCompatActivity() {
    lateinit var binding: ActivityUserDetailsAdminBinding
    private lateinit var firebaseauth: FirebaseAuth
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap
    var contex = this

    val storage = Firebase.storage
    val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDetailsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        var roles = ArrayList<String>()
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                roles = FireStore.getAllRoles()

                if(InterWindows.iwUser!!.picture != InterWindows.iwUser!!.email){
                    Log.d("SergioFoto", "Foto: " + InterWindows.iwUser!!.picture)
                    fileDownload(InterWindows.iwUser!!.picture!!)
                }else{
                    Log.d("SergioMail", "Foto: " + InterWindows.iwUser!!.email)
                    fileDownload(InterWindows.iwUser!!.email)
                }
            }
            job.join()
        }

        binding.cboRoleUserAdmin.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item , roles)
        binding.txtNameUserAdmin.setText(InterWindows.iwUser.name)
        binding.txtEmailUserAdmin.setText(InterWindows.iwUser.email)
        binding.txtAddressUserAdmin.setText(InterWindows.iwUser.address)
        binding.txtPhoneUserAdmin.setText(InterWindows.iwUser.phone)
        binding.cboRoleUserAdmin.setSelection(InterWindows.iwUser.role!!.toInt())


        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnSaveChangesUserAdmin.setOnClickListener {
            // Obtener el rol seleccionado del Spinner
            val selectedRole = binding.cboRoleUserAdmin.selectedItemPosition

            var name = binding.txtNameUserAdmin.text.toString().uppercase().trim()
            var email = binding.txtEmailUserAdmin.text.toString().uppercase().trim()
            var address = binding.txtAddressUserAdmin.text.toString().uppercase().trim()
            var phone = binding.txtPhoneUserAdmin.text.toString().uppercase().trim()
            var picture = InterWindows.iwUser.picture
            var role = selectedRole.toString()

            var user = User(
                name,
                email,
                address,
                phone,
                picture,
                role
            )
            user.batches = InterWindows.iwUser.batches

            uploadPictureOK()
            runBlocking {
                val job : Job = launch(context = Dispatchers.Default) {
                    FireStore.updateAllDataUser(user)
                }
                job.join()
                Toast.makeText(contex, R.string.changes_saved, Toast.LENGTH_SHORT).show()
            }
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
                    binding.pictureUserAdmin.setImageBitmap(imageBitmap)

                    Log.d("ComprobacionFuera",InterWindows.iwUser.email)

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
                        val Folder: StorageReference = FirebaseStorage.getInstance().getReference().child(Routes.usersPicturesPath)
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

        val imagesRef = storageRef.child(Routes.usersPicturesPath)
        var pictureName = binding.txtEmailUserAdmin.text.toString().uppercase().trim()

        InterWindows.iwUser.picture = InterWindows.iwUser.email

        val uploadTask = imagesRef.child(pictureName).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{

        }
    }


} // End of class UserDetailsAdmin_Controller