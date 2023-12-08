package Controllers.Classifier

import Adapters.RecyAdapterInventory
import Adapters.RecyAdapterItemsFromJewel
import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Store.AllBatchesDonor
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityAddJewelBinding
import com.example.jawaschallenge.databinding.ActivityInventoryBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Inventory_Controller : AppCompatActivity() {
    val storage = Firebase.storage
    val storageRef = storage.reference
    lateinit var binding: ActivityInventoryBinding
    private lateinit var firebaseauth: FirebaseAuth
    lateinit var miRecyclerView : RecyclerView
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        runBlocking {
            val trabajo: Job = launch(context = Dispatchers.Default) {
                InterWindows.inventory = FireStore.getItemsInventory()
            }
            trabajo.join()
        }

        miRecyclerView = binding.rVInvertory
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterInventory(InterWindows.inventory.sumarize.toMutableList(), context)
        miRecyclerView.adapter = miAdapter

        binding.seekBarAmount3.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // Display the current progress of SeekBar
                    binding.lblNumberAmount3.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Do something
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Do something
                }
            }
        )

        binding.btnHomeAdmin.setOnClickListener{
           finish()
        }

        binding.btnDeleteItem.setOnClickListener {
            if (InterWindows.iwItemInventory.name != "") {
                with(builder)
                {
                    setTitle("Vas a borrar ${binding.seekBarAmount3.progress} unidades de ${InterWindows.iwItemInventory.name}")
                    setMessage("¿Seguro que quieres continuar?")
                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->

                        runBlocking {
                            val trabajo : Job = launch(context = Dispatchers.Default) {

                                FireStore.deleteItemsByTypeAndQuantity(InterWindows.iwItemInventory.name, binding.seekBarAmount3.progress)

                            }
                            trabajo.join()
                            onResume()
                        }
                    }))
                    setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
//                                Toast.makeText(context, "Has pulsado no", Toast.LENGTH_SHORT).show()
                    }))
                    show()
                }
            }else{
                val innerBuilder = AlertDialog.Builder(this)
                innerBuilder.setTitle("No has seleccionado ningún item")
                innerBuilder.setMessage("Selecciona un item para poder borrarlo")
                innerBuilder.show()
            }
        }


    }// End of onCreate

    override fun onResume() {
        super.onResume()
        runBlocking {
            val trabajo: Job = launch(context = Dispatchers.Default) {
                InterWindows.inventory = FireStore.getItemsInventory()
            }
            trabajo.join()
        }
        miRecyclerView = binding.rVInvertory
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterInventory(InterWindows.inventory.sumarize.toMutableList(), context)
        miRecyclerView.adapter = miAdapter
    }

}// End of Class