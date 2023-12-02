package Controllers.Administrator

import Controllers.Jeweler.JewelsCrud_Controller
import Controllers.Accounts.Login_Controller
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityAdministratorElectionBinding
import com.google.firebase.auth.FirebaseAuth


class AdministratorElection_Controller : AppCompatActivity() {
    lateinit var binding: ActivityAdministratorElectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrator_election)

        binding = ActivityAdministratorElectionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserCrud_Controller::class.java)
            startActivity(inte)
        }

        binding.btnClassifer.setOnClickListener {
            var inte: Intent = Intent(this, JewelsCrud_Controller::class.java)
            startActivity(inte)
        }

        binding.btnDesigner.setOnClickListener {
            // Código para ItemsType_Controller
        }

        binding.btnDonor.setOnClickListener {
            // Código para CreateAccountEmail_Controller
        }

        binding.btnLogOut.setOnClickListener {
            signOutAndRedirectToLogin()
        }
    }

    fun signOutAndRedirectToLogin() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Login_Controller::class.java)
        startActivity(intent)
        finish()
    }
}
