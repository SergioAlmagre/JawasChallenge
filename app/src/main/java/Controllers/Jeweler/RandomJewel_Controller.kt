package Controllers.Jeweler

import Auxiliaries.InterWindows
import Connections.FireStore
import Model.Jewels.Jewel
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
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
//        setContentView(R.layout.activity_random_jewel)

        binding = ActivityRandomJewelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)


        binding.btnPlayRandom.setOnClickListener {

            runBlocking {
                val job : Job = launch(context = Dispatchers.Default) {
                    getAllJewels()
                    getAllPosibleJewels()
                    Log.d("Jewel", "Jewels: ${allJewels}")
                    Log.d("Jewel", "Posibles: ${posiblesJewels}")
                }
                job.join()
                searchJewelPromp(binding.txtPrompRandom.text.toString().uppercase().trim())
                Log.d("Jewel", "Posibles: ${posiblesJewels}")
                if(allJewels.size == 0){
                    binding.lblNameJewelRandom.text = "No hay joyas posibles"
                }else{
                    selectedJewel = finalJewels.random()
                }
                Log.d("Jewel", "Label: ${binding.lblNameJewelRandom.text}")

                binding.lblNameJewelRandom.text = selectedJewel.name
            }
        }

        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }


        binding.btnCreateJewel.setOnClickListener {
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




    }//End onCreate


    suspend fun getAllPosibleJewels(){
        for (jew in allJewels){
            if(checkIfDoJewelIsPosible(jew)){
                posiblesJewels.add(jew)
            }
        }
        Log.d("Jewel", "Posibles: ${posiblesJewels}")
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
        Log.d("Jewel","finalJewels: ${finalJewels}" )
    }

    suspend fun getAllJewels(){
        allJewels = FireStore.getAllObjetcJewel()
        Log.d("Jewel", "All Jewels: ${allJewels}")
    }



}//End of RandomJewel_Controller