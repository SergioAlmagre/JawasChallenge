package Controllers.Jeweler

import Auxiliaries.InterWindows
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityAddBatchBinding
import com.example.jawaschallenge.databinding.ActivityJewelInstructionsBinding
import com.google.firebase.auth.FirebaseAuth

class JewelInstructions_Controller : AppCompatActivity() {
    lateinit var binding: ActivityJewelInstructionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJewelInstructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lblJawelName.text = InterWindows.iwJewel.name

        binding.txtInstructions.setText(InterWindows.iwJewel.instructions.toString())
        Log.d("JewelInstructions", InterWindows.iwJewel.instructions.toString())


        binding.btnHomeAdmin.setOnClickListener {
            InterWindows.iwJewel.instructions = binding.txtInstructions.text.toString()
            Log.d("JewelInstructions", InterWindows.iwJewel.instructions.toString())
            Log.d("JewelInstructions", binding.txtInstructions.text.toString())
            Toast.makeText(this, "Instrucciones guardadas", Toast.LENGTH_SHORT).show()
            finish()
        }



    }// End of onCreate

}// End of class JewelInstructions_Controller