package Controllers.Administrator

import Adapters.RecyAdapterAdminUsers
import Auxiliaries.InterWindows
import Controllers.Accounts.CreateAccountEmail_Controller
import Controllers.Shared.UserDetails_Controller
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.databinding.ActivityCrudBinding
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UserCrud_Controller : AppCompatActivity() {
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
                InterWindows.iwUsersAL.clear()
                InterWindows.iwUsersAL = Connections.FireStore.getAllUsers()
            }
            trabajo.join()
        }

        miRecyclerView = binding.objetRecycler as RecyclerView
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterAdminUsers(InterWindows.iwUsersAL, context)
        miRecyclerView.adapter = miAdapter


        binding.btnHomeAdmin.setOnClickListener{
//            finish()
        }


        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }

        binding.btnAddObject.setOnClickListener {
            var inte: Intent = Intent(this, CreateAccountEmail_Controller::class.java)
            startActivity(inte)
        }
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                InterWindows.iwUsersAL.clear()
                InterWindows.iwUsersAL = Connections.FireStore.getAllUsers()
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            trabajo.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        miRecyclerView = binding.objetRecycler as RecyclerView
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterAdminUsers(InterWindows.iwUsersAL, context)
        miRecyclerView.adapter = miAdapter

    }
}