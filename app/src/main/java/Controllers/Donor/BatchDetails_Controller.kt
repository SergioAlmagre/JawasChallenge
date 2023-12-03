package Controllers.Donor

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Shared.UserDetails_Controller
import Model.Hardware.BatchInfo
import Store.AllBatchesDonor
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

        var selectedBatch:BatchInfo? = null

        runBlocking {
            val trabajo : Job = launch(context = Dispatchers.Default) {
                selectedBatch = FireStore.getBatchInfoById(InterWindows.iwUser.email,InterWindows.iwBatch.idBatch)
            }
            trabajo.join()
            if(selectedBatch != null){
                binding.lblBatchId.text = selectedBatch!!.batchID
                binding.lblNameDonoBatch.text = selectedBatch!!.userName
                binding.lblUserBatchEmail.text = selectedBatch!!.email
                binding.lblAddressBatch.text = selectedBatch!!.address
                binding.lblCreationDateBatch.text = selectedBatch!!.creationDate

                if (selectedBatch!!.isReceived) {
                    binding.lblSiNo.text= "SI"
                    binding.checkRecibed.setBackgroundColor(0xFFA9FF77.toInt())
                } else {
                    binding.lblSiNo.text= "NO"
                    binding.checkRecibed.setBackgroundColor(0xFFFF8A80.toInt())
                }

            }
            else{
                Log.d("BatchDetails","selectedBatch is null")
            }
        }


        binding.btnHomeAdmin.setOnClickListener{
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }






    }// End of onCreate



}// End of class BatchDetails_Controller