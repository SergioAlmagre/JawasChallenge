package Controllers.Jeweler

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Shared.UserDetails_Controller
import Model.Jewels.Jewel
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.jawaschallenge.databinding.ActivityRandomJewelBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File


class RandomJewel_Controller : AppCompatActivity() {
    lateinit var binding: ActivityRandomJewelBinding
    private lateinit var firebaseauth: FirebaseAuth
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap

    val storage = Firebase.storage
    val storageRef = storage.reference

    var allJewels: MutableList<Jewel> = mutableListOf()
    var posiblesJewels: MutableList<Jewel> = mutableListOf()
    var finalJewels: MutableList<Jewel> = mutableListOf()
    var selectedJewel = Jewel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRandomJewelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)


        binding.btnCreateJewel.setOnClickListener {
            if(selectedJewel.name.isEmpty()){
                Toast.makeText(this,"No hay joyas posibles que fabricar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                with(builder)
                {
                    setTitle("Vamos allá!")
                    setMessage("¿Quieres crear esta joya? \n Los items se descontarán de tu inventario")
                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                        runBlocking {
                            val trabajo : Job = launch(context = Dispatchers.Default) {
                                var selectedJewel = FireStore.getJewelByName(selectedJewel.name)
                                FireStore.deleteItemsForJewel(selectedJewel!!)
                            }
                            trabajo.join()
                            InterWindows.iwJewel = selectedJewel
                            var inte: Intent = Intent(context, AddJewel_Controller::class.java)
                            context.startActivity(inte)
                        }
                    }))
                    setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->

                    }))
                    show()
                }
            }

        }

        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }


        binding.btnRandomJewel.setOnClickListener {
            clearResults()
            Toast.makeText(this,"Buscando joya...", Toast.LENGTH_SHORT).show()
            runBlocking {
                val job : Job = launch(context = Dispatchers.Default) {
                    getAllJewels()
                    getAllPosibleJewels()
                }
                job.join()
                searchJewelPromp(binding.txtPrompRandom.text.toString().uppercase().trim())

                if(allJewels.size == 0){
                    binding.lblNameJewelRandom.text = "No hay joyas posibles"
                }else{
                    selectedJewel = finalJewels.random()
                    fileDownload(selectedJewel.picture)
                    Log.d("RandomJewel", "selected: ${selectedJewel}")
                }

                binding.lblNameJewelRandom.text = selectedJewel.name
            }
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }




    }//End onCreate

    fun clearResults(){
        allJewels.clear()
        posiblesJewels.clear()
        finalJewels.clear()
    }


    suspend fun getAllPosibleJewels(){
        for (jew in allJewels){
            if(checkIfDoJewelIsPosible(jew)){
                posiblesJewels.add(jew)
            }
        }
        Log.d("RandomJewel", "Posibles: ${posiblesJewels}")
    }

    suspend fun checkIfDoJewelIsPosible(jewel: Jewel): Boolean {
        var isPosible = false
        var actualInvertoryItems = FireStore.getItemsInventory()
        var quantityOfItems = 0

        for (component in actualInvertoryItems.sumarize) {
            for (componentJewel in jewel.components) {
                if (componentJewel.name == component.name) {
                    if (component.quantity >= componentJewel.quantity) {
                        isPosible = true
                        quantityOfItems++
                    } else {
                        isPosible = false
                        break
                    }
                }
            }
        }
        if(quantityOfItems != jewel.components.size) isPosible = false
        return isPosible
    }

    fun searchJewelPromp(promp: String){
        for(jew in posiblesJewels){
            if(jew.name.contains(promp)){
                finalJewels.add(jew)

            }
        }
        Log.d("RandomJewel","finalJewels: ${finalJewels}" )
    }

    suspend fun getAllJewels(){
        allJewels = FireStore.getAllObjetcJewel()
        Log.d("RandomJewel", "All Jewels: ${allJewels}")
    }

//    fun fileDownload(identificador: String?) {
//        var spaceRef = storageRef.child(Routes.jewelsPicturesPath + identificador)
//        val localfile = File.createTempFile(identificador!!, "jpg")
//        Log.d("fileDownload", "localfile: ${localfile.absolutePath}")
//        Log.d("fileDownload", "localfile: ${localfile.name}")
//        spaceRef.getFile(localfile).addOnSuccessListener {
//            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
//            binding.imageViewRandom.setImageBitmap(bitmap)
//        }.addOnFailureListener {
//            Log.d("fileDownload", "Error al descargar la imagen")
//        }
//    }

    fun fileDownload(identificador: String?) {
        var spaceRef = storageRef.child(Routes.jewelsPicturesPath + identificador)
        val localfile = File.createTempFile(identificador!!, "jpeg")
        spaceRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

            // Crear una máscara con esquinas redondeadas
            val roundedBitmap = getRoundedCornerBitmap(bitmap, 45f)

            // Mostrar la imagen redondeada en la ImageView
            binding.imageViewRandom.setImageBitmap(roundedBitmap)
        }.addOnFailureListener {
            // Manejo de errores
        }
    }

    private fun getRoundedCornerBitmap(bitmap: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawRoundRect(rectF, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }


}//End of RandomJewel_Controller