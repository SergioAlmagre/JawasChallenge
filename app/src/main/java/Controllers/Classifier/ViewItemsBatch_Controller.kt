package Controllers.Classifier

import Adapters.RecyAdapterItemsFromBatch
import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Shared.UserDetails_Controller
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.databinding.ActivityViewItemsBatchBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class ViewItemsBatch_Controller : AppCompatActivity() {
    lateinit var binding: ActivityViewItemsBatchBinding
    lateinit var miRecyclerView : RecyclerView
    private lateinit var firebaseauth: FirebaseAuth
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap
    var context = this
    var positionPicture = 2


    val storage = Firebase.storage
    val storageRef = storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewItemsBatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {

                InterWindows.iwItemsInside = FireStore.getItemsForBatchReceived(InterWindows.iwBatch.idBatch)
                Log.d("AddItemsClassifierOnCreate","iwItemsInside: ${InterWindows.iwBatch.idBatch}")
                Log.d("AddItemsClassifierOnCreate","iwItemsInside size: ${InterWindows.iwItemsInside.size}")
                Log.d("AddItemsClassifierOnCreate","iwItemsInside size: ${InterWindows.iwItemsInside}")

            }
            trabajo.join()
            miRecyclerView = binding.rVItemsInsideBatch
            miRecyclerView.setHasFixedSize(true)
            miRecyclerView.layoutManager = LinearLayoutManager(context)

            var miAdapter = RecyAdapterItemsFromBatch(InterWindows.iwItemsInside, context)
            miRecyclerView.adapter = miAdapter

        }


        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }


        binding.btnAddItem.setOnClickListener {
            var inte = Intent(this, AddItemBatch_Controller::class.java)
            startActivity(inte)
        }


        binding.btnEndBatchItems.setOnClickListener {
            with(builder)
            {
                setTitle("Esta acción es irreversible")
                setMessage("El lote se cerrará y no se podrán añadir más items. ¿Seguro que quieres continuar?")
                setPositiveButton(
                    "Si",
                    android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                        runBlocking {
                            val trabajo: Job = launch(context = Dispatchers.Default) {
                                var emailOwner = FireStore.getUserEmailByBatchId(InterWindows.iwBatch.idBatch)

                                FireStore.endBatch(emailOwner!!,InterWindows.iwBatch.idBatch)
                            }
                            trabajo.join()
                            finish()
                        }
                    })
                )
                setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
                }))
                show()
            }

        }

        //Como puedo detectar que se ha pulsado? // para poner la foto del item en el imageview
//        binding.rVItemsInsideBatch.setOnClickListener  {
//            Toast.makeText(this,"Click",Toast.LENGTH_SHORT).show()
//        }


    }// End onCreate

//    RecyAdapterItemsTxt.onItemClickListener = object : RecyAdapterItemsTxt.OnItemClickListener {
//        override fun onItemClick(position: Int) {
//            // Manejar el clic en el elemento aquí
//            Toast.makeText(context, "Clic en el elemento $position", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onResume() {
        super.onResume()
        if(InterWindows.iwItem.attributes[positionPicture].content != null){
            fileDownload(InterWindows.iwItem.attributes[positionPicture].content)
        }else{
            fileDownload(Routes.defaultItemPictureName)
        }

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                InterWindows.iwItemsInside.clear()
                InterWindows.iwItemsInside =  FireStore.getItemsForBatchReceived(InterWindows.iwBatch.idBatch)
            }
            trabajo.join()
            miRecyclerView = binding.rVItemsInsideBatch as RecyclerView
            miRecyclerView.setHasFixedSize(true)
            miRecyclerView.layoutManager = LinearLayoutManager(context)

            var miAdapter = RecyAdapterItemsFromBatch(InterWindows.iwItemsInside, context)
            miRecyclerView.adapter = miAdapter
        }
    }


    fun fileDownload(identificador: String?) {
        Log.d("FileDownload", "identificador: ${Routes.itemsPicturesPath + identificador}")
        var spaceRef = storageRef.child(Routes.itemsPicturesPath + identificador)
        val localfile = File.createTempFile(identificador!!, "jpg")
        spaceRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.pictureItem.setImageBitmap(bitmap)
        }.addOnFailureListener {

        }
    }

}// End class