package Controllers.Jeweler

import Adapters.RecyAdapterJeweler
import Auxiliaries.InterWindows
import Controllers.Shared.UserDetails_Controller
import Model.Jewels.Jewel
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

class JewelsCrud_Controller : AppCompatActivity() {
    lateinit var miRecyclerView : RecyclerView
    lateinit var binding: ActivityCrudBinding
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHomeAdmin.visibility = android.view.View.GONE

        var storage = com.google.firebase.ktx.Firebase.storage
        var storageRef = storage.reference

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {

                Connections.FireStore.getAllJewels()
            }
            trabajo.join()
        }

        miRecyclerView = binding.objetRecycler
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterJeweler(Store.JewelsCatalog.jewelsList, context)
        miRecyclerView.adapter = miAdapter


        binding.btnHomeAdmin.setOnClickListener{
//            finish()
        }


        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }

        binding.btnAddObject.setOnClickListener {
            InterWindows.iwJewel = Jewel()
            var inte: Intent = Intent(this, AddJewel_Controller::class.java)
            startActivity(inte)
        }

        binding.btnRandomJewel.setOnClickListener {
            var inte: Intent = Intent(this, RandomJewel_Controller::class.java)
            startActivity(inte)
        }

    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                Store.JewelsCatalog.jewelsList.clear()
                Connections.FireStore.getAllJewels()
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            trabajo.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        miRecyclerView = binding.objetRecycler as RecyclerView
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterJeweler(Store.JewelsCatalog.jewelsList, context)
        miRecyclerView.adapter = miAdapter

    }
}