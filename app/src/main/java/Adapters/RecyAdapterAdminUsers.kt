package Adapters


import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Administrator.UserDetailsAdmin_Controller
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory
import com.example.jawaschallenge.R
import com.google.firebase.Firebase
//import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.storage
import java.io.File
import Model.Users.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.widget.FrameLayout
import android.widget.ImageButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecyAdapterAdminUsers(var users : ArrayList<User>, var  context: Context) : RecyclerView.Adapter<RecyAdapterAdminUsers.ViewHolder>() {

    /**
     * onBindViewHolder() se encarga de coger cada una de las posiciones de la lista de personajes y pasarlas a la clase
     * ViewHolder(clase interna, ver abajo) para que esta pinte todos los valores y active el evento onClick en cada uno.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = users.get(position)
        holder.bind(item, context, position, this)
    }

    /**
     *  Como su nombre indica lo que hará será devolvernos un objeto ViewHolder al cual le pasamos la celda que hemos creado.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_card_users, parent, false)
        val viewHolder = ViewHolder(vista)

        viewHolder.itemView.setOnClickListener {

        }
        return viewHolder
    }

    /**
     * getItemCount() nos devuelve el tamaño de la lista, que lo necesita el RecyclerView.
     */
    override fun getItemCount(): Int {
        return users.size
    }

    //--------------------------------- Clase interna ViewHolder -----------------------------------
    /**
     * La clase ViewHolder. No es necesaria hacerla dentro del adapter, pero como van tan ligadas
     * se puede declarar aquí.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val mailUser = view.findViewById(R.id.txtInfo) as TextView
        val userPicture = view.findViewById(R.id.ObjetPicture) as ImageView

        val storage = Firebase.storage
        val storageRef = storage.reference
        val colorLayaoutReceived = view.findViewById(R.id.colorLayoutReceived) as FrameLayout
        val playIcon = view.findViewById(R.id.btnBuildJewel) as ImageButton


        /**
         * Éste método se llama desde el método onBindViewHolder de la clase contenedora. Como no vuelve a crear un objeto
         * sino que usa el ya creado en onCreateViewHolder, las asociaciones findViewById no vuelven a hacerse y es más eficiente.
         */
        @SuppressLint("ResourceAsColor")
        fun bind(
            usu: User,
            context: Context,
            pos: Int,
            miAdaptadorRecycler: RecyAdapterAdminUsers
        ) {
            val builder = AlertDialog.Builder(context)
            mailUser.text = usu.email
            fileDownload(usu.picture)
            colorLayaoutReceived.visibility = View.INVISIBLE
            playIcon.visibility = View.INVISIBLE


            itemView.setOnLongClickListener() {
                with(builder)
                {
                    setTitle("Delete")
                    setMessage("This will delete the user. Are you sure?")
                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->

                        runBlocking {
                            val trabajo : Job = launch(context = Dispatchers.Default) {
                                FireStore.deleteUserByEmail(usu.email) // Borrar usuario
                                if (usu.picture != Routes.defaultUserPictureName){
                                    FireStore.deleteImageFromStorage(usu.picture!!,Routes.usersPicturesPath) // Borrar imagen de usuario (si la tiene
                                }

                                InterWindows.iwUsersAL.remove(usu)
                            }
                            trabajo.join()
                            miAdaptadorRecycler.notifyDataSetChanged()
                        }
                    }))
                    setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
//                                Toast.makeText(context, "Has pulsado no", Toast.LENGTH_SHORT).show()
                    }))
                    show()
                }

                true
            }


            itemView.setOnClickListener {
                InterWindows.iwUser = InterWindows.iwUsersAL[pos] // valor dado por indice de pos en itemView desde ArrayList en Interventana

                if (InterWindows.iwUser != null){

                    var inte: Intent = Intent(context, UserDetailsAdmin_Controller::class.java)
                    context.startActivity(inte)
                }
            }


        }
//        fun fileDownload(identificador: String?) {
//            var spaceRef = storageRef.child(Routes.usersPicturesPath + identificador)
//            val localfile = File.createTempFile(identificador!!, "jpeg")
//            spaceRef.getFile(localfile).addOnSuccessListener {
//                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
//                userPicture.setImageBitmap(bitmap)
//            }.addOnFailureListener {
//
//            }
//        }

        fun fileDownload(identificador: String?) {
            var spaceRef = storageRef.child(Routes.usersPicturesPath + identificador)
            val localfile = File.createTempFile(identificador!!, "jpeg")
            spaceRef.getFile(localfile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                // Crear una máscara con esquinas redondeadas
                val roundedBitmap = getRoundedCornerBitmap(bitmap, 45f)

                // Mostrar la imagen redondeada en la ImageView
                userPicture.setImageBitmap(roundedBitmap)
            }.addOnFailureListener {
                // Manejo de errores
            }
        }

        private fun getRoundedCornerBitmap(bitmap: Bitmap, radius: Float): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)

            paint.isAntiAlias = true
            canvas.drawRoundRect(rectF, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }
    }


}
