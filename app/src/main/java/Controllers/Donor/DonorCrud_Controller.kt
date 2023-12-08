package Controllers.Donor

import Adapters.RecyAdapterDonor
import Auxiliaries.InterWindows
import Controllers.Shared.UserDetails_Controller
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.databinding.ActivityCrudBinding
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DonorCrud_Controller : AppCompatActivity() {
    lateinit var miRecyclerView : RecyclerView
    lateinit var binding: ActivityCrudBinding
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var storage = com.google.firebase.ktx.Firebase.storage
        var storageRef = storage.reference

        binding.btnHomeAdmin.visibility = android.view.View.GONE
        binding.btnRandomJewel.visibility = android.view.View.GONE

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                Log.d("recyclerBatchDentro",InterWindows.iwUser.email)
                Store.AllBatchesDonor.batchList = Connections.FireStore.getBatchesForUser(InterWindows.iwUser.email)
                Log.d("recyclerBatchDentro",Store.AllBatchesDonor.batchList.toString())

            }
            trabajo.join()
        }
        Log.d("recyclerBatchFuera",InterWindows.iwUser.email)
        Log.d("recyclerBatchFuera",Store.AllBatchesDonor.batchList.toString())

        miRecyclerView = binding.objetRecycler
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterDonor(Store.AllBatchesDonor.batchList, context)
        miRecyclerView.adapter = miAdapter


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
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                Store.AllBatchesDonor.batchList.clear()
                Store.AllBatchesDonor.batchList = Connections.FireStore.getBatchesForUser(InterWindows.iwUser.email)
            }
            trabajo.join()
        }
        miRecyclerView = binding.objetRecycler as RecyclerView
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterDonor(Store.AllBatchesDonor.batchList, context)
        miRecyclerView.adapter = miAdapter

    }
}