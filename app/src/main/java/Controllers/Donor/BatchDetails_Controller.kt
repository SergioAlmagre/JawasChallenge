package Controllers.Donor

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Shared.UserDetails_Controller
import Model.Hardware.Batch
import Model.Hardware.BatchInfo
import Store.AllBatchesDonor
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityBatchDetailsBinding
import com.example.jawaschallenge.databinding.ActivityUserDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BatchDetails_Controller : AppCompatActivity() {
    lateinit var binding: ActivityBatchDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_details)

        binding = ActivityBatchDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var context = this
        val builder = AlertDialog.Builder(context)

        if(InterWindows.iwUser.role == "3" || InterWindows.iwUser.role == "1"){
            binding.chkRecived.isVisible = true
        }

        var selectedBatch:BatchInfo? = null
        runBlocking {

                val trabajo: Job = launch(context = Dispatchers.Default) {
                    selectedBatch = FireStore.getBatchInfoById(
                        InterWindows.iwUser.email,
                        InterWindows.iwBatch.idBatch
                    )
                    selectedBatch!!.address = InterWindows.iwBatch.address.toString()
                }

                trabajo.join()
                if (selectedBatch != null) {
                    binding.lblBatchId.text = selectedBatch!!.batchID
                    binding.lblNameDonoBatch.text = selectedBatch!!.userName
                    binding.lblUserBatchEmail.text = selectedBatch!!.email
                    binding.lblAddressBatch.text = selectedBatch!!.address
                    binding.lblCreationDateBatch.text = selectedBatch!!.creationDate
                    binding.lblDescrObserva.text = InterWindows.iwBatch.aditionalInfo.toString()
                    Log.d("BatchDetails", selectedBatch.toString())
                    Log.d("BatchDetails", InterWindows.iwBatch.aditionalInfo.toString())
                } else {
                    Log.d("BatchDetails", "selectedBatch is null")
                }


            if(selectedBatch != null) {
                if (!selectedBatch!!.isReceived) {
                    binding.chkRecived.isChecked = false
                    binding.lblSiNo.text = "NO"
                    binding.colorLayoutReceived.setBackgroundColor(0xFFFF8A80.toInt())
                } else {
                    binding.chkRecived.isChecked = true
                    binding.lblSiNo.text = "SI"
                    binding.colorLayoutReceived.setBackgroundColor(0xFFA9FF77.toInt())
                }
            }

            if(InterWindows.iwUser.role == "3" || InterWindows.iwUser.role == "1") {
                binding.chkRecived.isVisible = true

                binding.chkRecived.setOnClickListener {
                    if (binding.chkRecived.isChecked) {

                        with(builder)
                        {
                            setTitle("Estas a punto de marcar como recibido un lote")
                            setMessage("¿Seguro que quieres continuar?")
                            setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                                binding.lblSiNo.text= "SI"
                                binding.colorLayoutReceived.setBackgroundColor(0xFFA9FF77.toInt())

                                InterWindows.iwBatch.received = true
                                runBlocking {
                                    val trabajo : Job = launch(context = Dispatchers.Default) {
                                        FireStore.addOrUpdateBatchToDonor(InterWindows.iwUser.email,InterWindows.iwBatch)
                                    }
                                    trabajo.join()
                                }
                            }))
                            setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
                                binding.chkRecived.isChecked = false
                            }))
                            show()
                        }

                    } else {

                        with(builder)
                        {
                            setTitle("Estas a punto de desmarcar como recibido un lote")
                            setMessage("¿Seguro que quieres continuar?")
                            setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                                binding.lblSiNo.text= "NO"
                                binding.colorLayoutReceived.setBackgroundColor(0xFFFF8A80.toInt())

                                InterWindows.iwBatch.received = false
                                runBlocking {
                                    val trabajo : Job = launch(context = Dispatchers.Default) {
                                        FireStore.addOrUpdateBatchToDonor(InterWindows.iwUser.email,InterWindows.iwBatch)
                                    }
                                    trabajo.join()
                                }
                            }))
                            setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
                                binding.chkRecived.isChecked = true
                            }))
                            show()
                        }

                    }
                }

            }

        }//End of runBlocking



        binding.btnHomeAdmin.setOnClickListener{
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }


    }// End of onCreate



}// End of class BatchDetails_Controller