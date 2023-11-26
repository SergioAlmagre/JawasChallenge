package Controllers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityLoginBinding
import com.example.jawaschallenge.databinding.ActivityWelcomeBinding

class Welcome_Controller : AppCompatActivity() {
    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_welcome)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnComeBackLogin.setOnClickListener {
            finish()
        }

        binding.btnGuideContact.setOnClickListener {
            goGuideContact()
        }



    }


    private fun goGuideContact() {
        val homeIntent =  Intent(this, GuideContact_Controller::class.java)
        startActivity(homeIntent)
    }


}