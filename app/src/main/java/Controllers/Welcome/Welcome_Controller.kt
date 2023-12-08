package Controllers.Welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jawaschallenge.databinding.ActivityWelcomeBinding

class Welcome_Controller : AppCompatActivity() {
    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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