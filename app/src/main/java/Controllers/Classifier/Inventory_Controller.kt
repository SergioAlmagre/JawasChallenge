package Controllers.Classifier

import Adapters.RecyAdapterItemsFromJewel
import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Store.AllBatchesDonor
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

        var miAdapter = RecyAdapterItemsFromJewel(InterWindows.inventory.sumarize.toMutableList(), context)
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

        var miAdapter = RecyAdapterItemsFromJewel(InterWindows.inventory.sumarize.toMutableList(), context)
        miRecyclerView.adapter = miAdapter
    }

}// End of Class