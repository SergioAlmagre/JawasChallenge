package Controllers.Jeweler

import Adapters.RecyAdapterItemsFromJewel
import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Administrator.UserDetailsAdmin_Controller
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jawaschallenge.databinding.ActivityAddJewelBinding
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

class AddJewel_Controller : AppCompatActivity() {
    lateinit var binding: ActivityAddJewelBinding
    private lateinit var firebaseauth: FirebaseAuth
    lateinit var miRecyclerView : RecyclerView
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap
    var context = this

    val storage = Firebase.storage
    val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddJewelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        loadData()

        miRecyclerView = binding.rVItemsFromJewel
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterItemsFromJewel(InterWindows.iwJewel.components, context)
        miRecyclerView.adapter = miAdapter


        binding.btnAddPhotoJewel.setOnClickListener {
            if(binding.txtJewelName.text.isNullOrEmpty()){
                val innerBuilder = AlertDialog.Builder(this)
                innerBuilder.setTitle("¿Cual es el nombre de la joya?")
                innerBuilder.setMessage("Antes de subir la foto debes ponerle un nombre a la joya")
                innerBuilder.show()
            }else{
                InterWindows.iwJewel.name = binding.txtJewelName.text.toString().uppercase().trim()
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
        }



        binding.btnAddInstructions.setOnClickListener {
            InterWindows.iwJewel.instructions = binding.txtInstructionsMain.text.toString()
            Log.d("AddJewel", "InterWindows.iwJewel.instructions: ${InterWindows.iwJewel.instructions}")
            val intent = Intent(this, JewelInstructions_Controller::class.java)
            startActivity(intent)
        }

        binding.btnAddItemToJewel.setOnClickListener {

            val intent = Intent(this, AddItemToJewel_Controller::class.java)
            startActivity(intent)
        }

        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            val intent = Intent(this, UserDetailsAdmin_Controller::class.java)
            startActivity(intent)
        }

        //GOOD BUT NOT ENOUGH
        binding.txtJewelName.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                if(checkIfNameOfJewelIsUnique(binding.txtJewelName.text.toString().uppercase().trim())) {
                }else{
                    val innerBuilder = AlertDialog.Builder(this)
                    innerBuilder.setTitle("¿Nombre repetido?")
                    innerBuilder.setMessage("El nombre de esta joya ya existe!\n, ¿le ponemos otro nombre?")
                    innerBuilder.show()
                }
            }
        }


        binding.btnEndJewel.setOnClickListener {
            Log.d("AddJewelBtnEndJewel", "InterWindows.iwJewel.name: ${InterWindows.iwJewel.name}")
            if(isModifyCorrect()){
                if(InterWindows.iwJewel.picture != Routes.defaultJewelPictureName){
                    uploadPictureOK()

                }

                FireStore.addJewelToCatalog(InterWindows.iwJewel)
                Toast.makeText(this, "Joya añadida!", Toast.LENGTH_SHORT).show()
                finish()
            }else{

            }
        }



    }//End onCreate

    fun isModifyCorrect(): Boolean {
        var isCorrect = true
        if (binding.txtJewelName.text.toString() != InterWindows.iwJewel.name) {
            if (!checkIfNameOfJewelIsUnique(binding.txtJewelName.text.toString().uppercase().trim()))
            {
                isCorrect = false
                val innerBuilder = AlertDialog.Builder(this)
                innerBuilder.setTitle("Nombre repetido")
                innerBuilder.setMessage("El nombre de esta joya ya existe!\n, ¿Le ponemos otro nombre?")
                innerBuilder.show()
            }else{ //Jewel modified
                runBlocking {
                    val trabajo : Job = launch(context = Dispatchers.Default) {
                        FireStore.deleteJewelByName(InterWindows.iwJewel.name)
                    }
                    trabajo.join()
                }
                InterWindows.iwJewel.name = binding.txtJewelName.text.toString().uppercase().trim()
                Log.d("AddJewelIsModi", "InterWindows.iwJewel.name: ${InterWindows.iwJewel.name}")
            }
        }else{
            InterWindows.iwJewel.name = binding.txtJewelName.text.toString().uppercase().trim()
            Log.d("AddJewelIsModi2", "InterWindows.iwJewel.name: ${InterWindows.iwJewel.name}")
        }
        if(InterWindows.iwJewel.components.size == 0){
            isCorrect = false
            val innerBuilder = AlertDialog.Builder(this)
            innerBuilder.setTitle("¿Componentes?")
            innerBuilder.setMessage("No has añadido ningún componente a la joya")
            innerBuilder.show()
        }
        return isCorrect
    }

    fun thereAreChanges():Boolean{
        var changes = true


        return changes
    }

    fun checkIfNameOfJewelIsUnique(nameJewel: String): Boolean {
        var isUnique = true
        for (jewelInCatalog in Store.JewelsCatalog.jewelsList) {
            if (jewelInCatalog.name == nameJewel) {
                isUnique = false
                break
            }
        }
        return isUnique
    }


    fun loadData(){
        if(InterWindows.iwJewel.name.isNullOrEmpty()) {
            InterWindows.iwJewel.picture = Routes.defaultJewelPictureName
        }else{
            Log.d("loadData", "InterWindows.iwJewel.name: ${InterWindows.iwJewel.name}")
            binding.txtJewelName.setText(InterWindows.iwJewel.name.uppercase().trim())
            binding.txtInstructionsMain.setText(InterWindows.iwJewel.instructions.toString())
            fileDownload(InterWindows.iwJewel.picture)
            Log.d("loadData", "InterWindows.iwJewel.picture: ${InterWindows.iwJewel.picture}")
        }
    }

    override fun onResume() {
        super.onResume()
//        if(!InterWindows.iwJewel.picture.isNullOrEmpty()){
//            fileDownload(InterWindows.iwJewel.picture)
//        }else{
//            fileDownload(Routes.defaultJewelPictureName)
//        }

        binding.txtInstructionsMain.setText(InterWindows.iwJewel.instructions.toString())
        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
//                InterWindows.iwItemsInside.clear()
//                InterWindows.iwItemsInside =  FireStore.getItemsForBatchReceived(InterWindows.iwBatch.idBatch)
            }
            trabajo.join()
            miRecyclerView = binding.rVItemsFromJewel
            miRecyclerView.setHasFixedSize(true)
            miRecyclerView.layoutManager = LinearLayoutManager(context)

            var miAdapter = RecyAdapterItemsFromJewel(InterWindows.iwJewel.components, context)
            miRecyclerView.adapter = miAdapter
        }
    }


    fun fileDownload(identificador: String?) {
        var spaceRef = storageRef.child(Routes.jewelsPicturesPath + identificador)
        val localfile = File.createTempFile(identificador!!, "jpg")
        Log.d("fileDownload", "localfile: ${localfile.absolutePath}")
        Log.d("fileDownload", "localfile: ${localfile.name}")
        spaceRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.pictureJewel.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.d("fileDownload", "Error al descargar la imagen")
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
                    binding.pictureJewel.setImageBitmap(imageBitmap)
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    InterWindows.iwJewel.picture = InterWindows.iwJewel.name.uppercase().trim()

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
                                // Sube el ByteArray a Firebase Storage
                                val Folder: StorageReference =
                                    FirebaseStorage.getInstance().reference.child(Routes.jewelsPicturesPath)
                                val file_name: StorageReference = Folder.child(InterWindows.iwJewel.name)

                                file_name.putBytes(byteArray)
                                    .addOnSuccessListener { taskSnapshot ->
                                        file_name.downloadUrl.addOnSuccessListener { uri ->
                                            // La imagen se subió correctamente y puedes obtener la URL de descarga
                                            InterWindows.iwJewel.picture = InterWindows.iwJewel.name.uppercase().trim()
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
            InterWindows.iwJewel.picture = InterWindows.iwJewel.name.uppercase().trim()
            binding.pictureJewel.setImageURI(uri)// Coloca la imagen en el pictureBox
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
                        val Folder: StorageReference = FirebaseStorage.getInstance().getReference().child(
                            Routes.jewelsPicturesPath)
                        val file_name: StorageReference = Folder.child(InterWindows.iwJewel.name)
                        file_name.putBytes(byteArray)
                            .addOnSuccessListener { taskSnapshot ->
                                file_name.downloadUrl.addOnSuccessListener { uri ->
                                    // La imagen se subió correctamente y puedes obtener la URL de descarga
//                                    Conexion.actualizarDocumento(u!!.mail, u!!.mail)
//                                    InterWindows.iwJewel.picture = InterWindows.iwJewel.name
                                }
                            }
                    }
                })
        } else {
            Log.d("Sergio", "No media selected")
        }
    }


    fun uploadPictureOK(){
        binding.pictureJewel.isDrawingCacheEnabled = true
        binding.pictureJewel.buildDrawingCache()
        val bitmap = (binding.pictureJewel.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
        val data2 = baos.toByteArray()

        InterWindows.iwJewel.picture = InterWindows.iwJewel.name.uppercase().trim()
        val imagesRef = storageRef.child(Routes.jewelsPicturesPath)
        var pictureName = InterWindows.iwJewel.picture

        val uploadTask = imagesRef.child(pictureName!!).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{
            //Toast.makeText(this@Registro, "Error en la subida de la imagen", Toast.LENGTH_SHORT).show()
        }
    }


}// End class