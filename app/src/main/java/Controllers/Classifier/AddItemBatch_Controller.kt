package Controllers.Classifier

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Shared.UserDetails_Controller
import Model.Hardware.Item
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jawaschallenge.databinding.ActivityAddItemBatchBinding
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

class AddItemBatch_Controller : AppCompatActivity() {
    lateinit var binding: ActivityAddItemBatchBinding
    private lateinit var firebaseauth: FirebaseAuth
    var context = this
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap

    val storage = Firebase.storage
    val storageRef = storage.reference

    var newItem = Item()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddItemBatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)
        loadAutocomplete()
        InterWindows.iwItem.attributes[Routes.picturePositionAttribute].content = Routes.defaultItemPictureName

        binding.seekBarAmount.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                // Display the current progress of SeekBar
                    binding.lblNumberAmount.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
                }
            }
        )

        binding.btnEndItem.setOnClickListener {

            var typeItem = binding.txtTypeItem.text.toString()
            if (Store.ItemsTypes.allTypesList.contains(typeItem)) {
                var amountItem = binding.lblNumberAmount.text.toString().toInt()
                var descriptionItem = binding.txtDescription.text.toString()

                for (i in 0..amountItem - 1) {
                    runBlocking {
                        val trabajo: Job = launch(context = Dispatchers.Default) {
                            newItem.attributes[Routes.typeNamePositionAttribute].content = typeItem
                            newItem.attributes[Routes.descriptionPositionAttribute].content = descriptionItem
                            newItem.attributes[Routes.picturePositionAttribute].content = InterWindows.iwItem.attributes[Routes.picturePositionAttribute].content
                            FireStore.addItemToBatch(
                                InterWindows.iwUser.email,
                                InterWindows.iwBatch.idBatch,
                                newItem
                            )
                        }
                        trabajo.join()
                        uploadPictureOK()
                        InterWindows.iwItem = newItem
                    }
                }
                loadAutocomplete()
                finish()
            }else{
                val innerBuilder = AlertDialog.Builder(this)
                innerBuilder.setTitle("Tipo de item no válido")
                innerBuilder.setMessage("Selecciona uno de los tipos de item que aparecen cunado escribes")
                innerBuilder.show()
            }
        }

        binding.btnAddPhotoEm3.setOnClickListener {
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


        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }



    }// End onCreate

    fun loadAutocomplete(){
        runBlocking {
            val trabajo: Job = launch(context = Dispatchers.Default) {
                FireStore.getAllDistinctTypes()
            }
            trabajo.join()
            Log.d("AddItemsClassifierOnCreate", "iwItemsInside: ${Store.ItemsTypes.allTypesList}")
            // Crear un adaptador con la lista de sugerencias
            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, Store.ItemsTypes.allTypesList)

            // Obtener la referencia al AutoCompleteTextView
            val autoCompleteTextView = binding.txtTypeItem

            // Establecer el adaptador en el AutoCompleteTextView
            autoCompleteTextView.setAdapter(adapter)

            // Manejar eventos cuando el usuario selecciona un elemento
            autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                val selectedType = parent.getItemAtPosition(position) as String
                // Hacer algo con el tipo seleccionado
            }
        }
    }


//__________________________CAMERA_______________________________
//Segunda activity para lanzar la cámara.
    val openCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    // La imagen capturada está en el objeto `imageBitmap`
                    this.bitmap = imageBitmap
                    binding.pictureItemAddBatch.setImageBitmap(imageBitmap)
                    InterWindows.iwItem.attributes[Routes.picturePositionAttribute].content = newItem.idItem //Here is where change the name of the picture when the camera is open!!!!!!!

                    //from here not work fine this function
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
                                    FirebaseStorage.getInstance().reference.child(Routes.itemsPicturesPath)
                                val file_name: StorageReference = Folder.child(newItem.idItem)

                                file_name.putBytes(byteArray)
                                    .addOnSuccessListener { taskSnapshot ->
                                        file_name.downloadUrl.addOnSuccessListener { uri ->
                                            // La imagen se subió correctamente y puedes obtener la URL de descarga
                                            InterWindows.iwItem.attributes[Routes.picturePositionAttribute].content = newItem.idItem

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
            binding.pictureItemAddBatch.setImageURI(uri)// Coloca la imagen en el pictureBox
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
                        val Folder: StorageReference = FirebaseStorage.getInstance().getReference().child(Routes.itemsPicturesPath)
                        val file_name: StorageReference = Folder.child(newItem.idItem)
                        file_name.putBytes(byteArray)
                            .addOnSuccessListener { taskSnapshot ->
                                file_name.downloadUrl.addOnSuccessListener { uri ->
                                    // La imagen se subió correctamente y puedes obtener la URL de descarga

                                    InterWindows.iwItem.attributes[Routes.picturePositionAttribute].content = newItem.idItem
                                }
                            }
                    }
                })
        } else {
            Log.d("Sergio", "No media selected")
        }
    }

    fun uploadPictureOK(){
        binding.pictureItemAddBatch.isDrawingCacheEnabled = true
        binding.pictureItemAddBatch.buildDrawingCache()
        val bitmap = (binding.pictureItemAddBatch.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data2 = baos.toByteArray()

        val imagesRef = storageRef.child(Routes.itemsPicturesPath)
        var pictureName = newItem.idItem

        InterWindows.iwItem.attributes[Routes.picturePositionAttribute].content = newItem.idItem

        val uploadTask = imagesRef.child(pictureName).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{

        }
    }



}// End class AddItemBatch_Controller


