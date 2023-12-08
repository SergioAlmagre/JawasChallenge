package Controllers.Jeweler

import Auxiliaries.InterWindows
import Auxiliaries.ObjectQuantity
import Connections.FireStore
import Controllers.Shared.UserDetails_Controller
import Store.ItemsTypes
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityAddItemToJewelBinding
import com.example.jawaschallenge.databinding.ActivityAddJewelBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AddItemToJewel_Controller : AppCompatActivity() {
    lateinit var binding: ActivityAddItemToJewelBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_to_jewel)

        binding = ActivityAddItemToJewelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val builder = AlertDialog.Builder(this)
        binding.lblJawelName2.text = InterWindows.iwJewel.name

        loadCombo()

        binding.cboItemsType2.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item , ItemsTypes.allTypesList)

        binding.seekBarAmount2.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // Display the current progress of SeekBar
                    binding.lblNumberAmount2.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Do something
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Do something
                }
            }
        )

        binding.btnAddItemAndAmound.setOnClickListener {
            with(builder)
            {
                setTitle("Añadir items a la joya")
                setMessage("¿Seguro que quieres añadir estos ítmes?")
                setPositiveButton(
                    "Si",
                    android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                        var objetCuantity = ObjectQuantity(binding.cboItemsType2.selectedItem.toString(),binding.seekBarAmount2.progress)
                        InterWindows.iwJewel.components.add(objetCuantity)
                        Toast.makeText(context, "Item añadido!", Toast.LENGTH_SHORT).show()
                        restart()
                        Log.d("AddItemToJewel", "InterWindows.iwJewel.components: ${InterWindows.iwJewel.components}")
                    })
                )
                setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->

                }))
                show()
            }
        }

        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }





    }//End onCreate

    fun loadCombo(){
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                FireStore.getAllDistinctTypes()
            }
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        binding.cboItemsType2.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item , Store.ItemsTypes.allTypesList)
    }

    fun restart(){
        binding.cboItemsType2.setSelection(0)
        binding.seekBarAmount2.progress = 0
    }

}// End class AddItemToJewel_Controller