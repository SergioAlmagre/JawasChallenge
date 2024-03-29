package Controllers.Classifier

import Adapters.RecyAdapterClassifier
import Auxiliaries.InterWindows
import Controllers.Donor.AddBatch_Controller
import Controllers.Shared.UserDetails_Controller
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.databinding.ActivityCrudBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ClassifierCrud_Controller : AppCompatActivity() {
    lateinit var miRecyclerView : RecyclerView
    lateinit var binding: ActivityCrudBinding
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHomeAdmin.visibility = android.view.View.GONE
        binding.btnRandomJewel.visibility = android.view.View.GONE

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                InterWindows.iwPendingBatches.clear()
                Connections.FireStore.getAllPendingBatchesFromUsers()
            }
            trabajo.join()
            miRecyclerView = binding.objetRecycler
            miRecyclerView.setHasFixedSize(true)
            miRecyclerView.layoutManager = LinearLayoutManager(context)

            var miAdapter = RecyAdapterClassifier(InterWindows.iwPendingBatches, context)
            Log.d("recyclerBatch",InterWindows.iwPendingBatches.toString())
            miRecyclerView.adapter = miAdapter
        }


        binding.btnHomeAdmin.setOnClickListener{
//            finish()
        }


        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }

        binding.btnAddObject.setOnClickListener {
            var inte: Intent = Intent(this, AddBatch_Controller::class.java)
            startActivity(inte)
        }

        binding.btnInventory.setOnClickListener {
            var inte: Intent = Intent(this, Inventory_Controller::class.java)
            startActivity(inte)
        }

    } // End onCreate

    override fun onResume() {
        super.onResume()
        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                InterWindows.iwPendingBatches.clear()
                Connections.FireStore.getAllPendingBatchesFromUsers()
            }
            trabajo.join()
        }
        miRecyclerView = binding.objetRecycler as RecyclerView
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterClassifier(InterWindows.iwPendingBatches, context)
        Log.d("recyclerBatch",InterWindows.iwPendingBatches.toString())
        miRecyclerView.adapter = miAdapter
    }

}// End Class