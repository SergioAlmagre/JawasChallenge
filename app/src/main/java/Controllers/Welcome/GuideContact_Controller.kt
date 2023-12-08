package Controllers.Welcome

import Constants.Routes
import Controllers.Accounts.Login_Controller
import Controllers.Shared.UserDetails_Controller
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityGuideContactBinding
import com.example.jawaschallenge.databinding.ActivityViewItemsBatchBinding
import com.google.firebase.auth.FirebaseAuth

class GuideContact_Controller : AppCompatActivity() {
    lateinit var binding: ActivityGuideContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuideContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContactAdministrator.setOnClickListener {
            sendEmail(this,Routes.adminitratorEmail)
        }

        binding.btnGoLogin.setOnClickListener {
            var inte = Intent(this, Login_Controller::class.java)
            startActivity(inte)
        }

        binding.btnMakeDonation.setOnClickListener {
            val innerBuilder = AlertDialog.Builder(this)
            innerBuilder.setTitle("Gracias por ayudar a nuestra organización")
            innerBuilder.setMessage("NUESTROBANCO\tES6621000418401234567891\t1210\t0418\t40\t1234567891")
            innerBuilder.show()
        }


    }// End of onCreate

    fun sendEmail(context: Context, emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // Esto asegura que solo las aplicaciones de correo respondan
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Asunto del correo")

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Manejar la excepción si no hay aplicaciones de correo instaladas
            Toast.makeText(context, "No hay aplicaciones de correo instaladas", Toast.LENGTH_SHORT).show()
        }
    }


}// End of class GuideContact_Controller