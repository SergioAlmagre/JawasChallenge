package Controllers.Administrator

import Connections.FireStore
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityItemsTypeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ItemsType_Controller : AppCompatActivity() {
    lateinit var binding: ActivityItemsTypeBinding
    private lateinit var firebaseauth: FirebaseAuth
    var contex = this

    val storage = Firebase.storage
    val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityItemsTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        loadCombo()

        binding.cboItemsType.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item , Store.ItemsTypes.allTypesList)

        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnAddObject.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("¿Como se llama el TIPO de item?")
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editText)
            builder.setView(dialogLayout)

            builder.setPositiveButton("OK") { dialogInterface, i ->
                val newItemTypeName = editText.text.toString()
                Toast.makeText(applicationContext, "$newItemTypeName introducido con éxito", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    try {
                        FireStore.addNewTypeToFirebase(newItemTypeName)
                        loadCombo()
                    } catch (e: Exception) {
                        // Manejar excepciones si es necesario
                        Log.e("AddNewType", "Error: $e")
                    }
                }
            }
            builder.show()
        }


        binding.btnDeleteType.setOnClickListener {
            var nameItem = binding.cboItemsType.selectedItem.toString()
            with(builder)
            {
                setTitle("Estas a punto de borrar un tipo de item")
                setMessage("¿Seguro que deseas continuar?")
                setPositiveButton(
                    "Si",
                    android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                        runBlocking {
                            val job : Job = launch(context = Dispatchers.Default) {
                                FireStore.deleteItemsTypeByName(binding.cboItemsType.selectedItem.toString())

                            }
                            job.join()
                            loadCombo()

                        }
                        Toast.makeText(contex,"Elemento ${nameItem} eliminado", Toast.LENGTH_SHORT).show()

                    })
                )
                setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
                    Toast.makeText(contex,"Proceso cancelado", Toast.LENGTH_SHORT).show()

                }))
                show()
            }
        }



    }// End of onCreate

    fun loadCombo(){
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                FireStore.getAllDistinctTypes()
            }
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        binding.cboItemsType.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item , Store.ItemsTypes.allTypesList)
    }


} // End of class UserDetailsAdmin_Controller