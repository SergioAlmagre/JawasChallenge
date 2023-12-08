package Controllers.Administrator

import Adapters.RecyAdapterAdminUsers
import Auxiliaries.InterWindows
import Controllers.Accounts.CreateAccountEmail_Controller
import Controllers.Shared.UserDetails_Controller
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityCrudBinding
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

        binding.btnHomeAdmin.visibility = android.view.View.GONE
        binding.btnRandomJewel.visibility = android.view.View.GONE
        binding.btnInventory.visibility = android.view.View.GONE


        if(InterWindows.iwUser.role == "1"){
            binding.btnAddObject.setImageResource(R.drawable.menu)
        }

        runBlocking {
            val trabajo: Job = launch(context = Dispatchers.Default) {
                InterWindows.iwUsersAL.clear()
                InterWindows.iwUsersAL = Connections.FireStore.getAllUsers()
            }
            trabajo.join()
        }

        miRecyclerView = binding.objetRecycler
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterAdminUsers(InterWindows.iwUsersAL, context)
        miRecyclerView.adapter = miAdapter


        binding.btnHomeAdmin.setOnClickListener {
//            finish()
        }


        binding.btnUserAdmin.setOnClickListener {
            var inte = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }


        binding.btnAddObject.setOnClickListener {
            val items = arrayOf("Añadir un usuario", "Gesitonar tipos de Items")
            var selectedItem = -1 // Variable para almacenar la posición del elemento seleccionado
            val builder = AlertDialog.Builder(this)

            builder.setTitle("¿Que quieres hacer?")
            builder.setSingleChoiceItems(items, selectedItem) { dialog, which ->
                // Actualiza la posición del elemento seleccionado
                selectedItem = which
            }

            builder.setPositiveButton("Go!") { dialogInterface, i ->
                if (selectedItem != -1) {
                    // Selecciona solo un elemento y realiza acciones según sea necesario
                    val selectedString = items[selectedItem]
                    if (selectedString == "Añadir un usuario") {
                        var inte = Intent(this, CreateAccountEmail_Controller::class.java)
                        startActivity(inte)
                    } else if (selectedString == "Gesitonar tipos de Items") {
                        var inte = Intent(this, ItemsType_Controller::class.java)
                        startActivity(inte)
                    } else {

                    }
                }
            }
            builder.show()
        }


    }//End of onCreate

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
        miRecyclerView = binding.objetRecycler
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = LinearLayoutManager(context)

        var miAdapter = RecyAdapterAdminUsers(InterWindows.iwUsersAL, context)
        miRecyclerView.adapter = miAdapter

    }
}// End of class