package Controllers.Classifier

import Adapters.RecyAdapterClassifier
import Auxiliaries.InterWindows
import Controllers.Donor.AddBatch_Controller
import Controllers.Shared.UserDetails_Controller
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.databinding.ActivityCrudBinding
import com.google.firebase.storage.ktx.storage
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

        var storage = com.google.firebase.ktx.Firebase.storage
        var storageRef = storage.reference

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {

                Connections.FireStore.getAllPendingBatchesFromUsers()
            }
            trabajo.join()
            miRecyclerView = binding.objetRecycler
            miRecyclerView.setHasFixedSize(true)
            miRecyclerView.layoutManager = LinearLayoutManager(context)

            var miAdapter = RecyAdapterClassifier(InterWindows.iwPendingBatches, context)
            miRecyclerView.adapter = miAdapter

        }


        binding.btnHomeAdmin.setOnClickListener{
            finish()
        }


        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }

        binding.btnAddObject.setOnClickListener {
            var inte: Intent = Intent(this, AddBatch_Controller::class.java)
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
        miRecyclerView.adapter = miAdapter

    }
}