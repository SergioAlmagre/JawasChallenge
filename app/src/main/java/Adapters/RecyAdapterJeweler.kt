package Adapters


import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Jeweler.AddJewel_Controller
import Model.Jewels.Jewel
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
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecyAdapterJeweler(var jewels : MutableList<Jewel>, var  context: Context) : RecyclerView.Adapter<RecyAdapterJeweler.ViewHolder>() {

    /**
     * onBindViewHolder() se encarga de coger cada una de las posiciones de la lista de personajes y pasarlas a la clase
     * ViewHolder(clase interna, ver abajo) para que esta pinte todos los valores y active el evento onClick en cada uno.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = jewels.get(position)
        holder.bind(item, context, position, this)
    }

    /**
     *  Como su nombre indica lo que hará será devolvernos un objeto ViewHolder al cual le pasamos la celda que hemos creado.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_card_users, parent, false)
        val viewHolder = ViewHolder(vista)

        // Configurar el OnClickListener
        viewHolder.itemView.setOnClickListener {

        }
        return viewHolder
    }

    /**
     * getItemCount() nos devuelve el tamaño de la lista, que lo necesita el RecyclerView.
     */
    override fun getItemCount(): Int {
        return jewels.size
    }

    //--------------------------------- Clase interna ViewHolder -----------------------------------
    /**
     * La clase ViewHolder. No es necesaria hacerla dentro del adapter, pero como van tan ligadas
     * se puede declarar aquí.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mailUser = view.findViewById(R.id.txtInfo) as TextView
        val userPicture = view.findViewById(R.id.ObjetPicture) as ImageView
        val colorLayaoutIsPosible = view.findViewById(R.id.colorLayoutReceived) as FrameLayout
        val playIcon = view.findViewById(R.id.btnBuildJewel) as ImageButton



        val storage = Firebase.storage
        val storageRef = storage.reference

        /**
         * Éste método se llama desde el método onBindViewHolder de la clase contenedora. Como no vuelve a crear un objeto
         * sino que usa el ya creado en onCreateViewHolder, las asociaciones findViewById no vuelven a hacerse y es más eficiente.
         */
        @SuppressLint("ResourceAsColor")
        fun bind(
            jew: Jewel,
            context: Context,
            pos: Int,
            miAdaptadorRecycler: RecyAdapterJeweler
        ) {
            val builder = AlertDialog.Builder(context)
            mailUser.text = jew.name
            fileDownload(jew.picture)
            colorLayaoutIsPosible.visibility = View.INVISIBLE


            runBlocking {
                val trabajo : Job = launch(context = Dispatchers.Default) {
                    if(checkIfDoJewelIsPosible(jew)){
                        playIcon.visibility = View.VISIBLE
                    }else{
                        playIcon.visibility = View.INVISIBLE
                    }
                }
                trabajo.join()
            }

            playIcon.setOnClickListener{
                with(builder)
                {
                    setTitle("Vamos allá!")
                    setMessage("¿Quieres crear esta joya? \n Los items se descontarán de tu inventario")
                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                        runBlocking {
                            val trabajo : Job = launch(context = Dispatchers.Default) {
                                var selectedJewel = FireStore.getJewelByName(jew.name)
                                FireStore.deleteItemsForJewel(selectedJewel!!)
                            }
                            trabajo.join()
                            InterWindows.iwJewel = Store.JewelsCatalog.jewelsList[pos]
                            var inte: Intent = Intent(context, AddJewel_Controller::class.java)
                            context.startActivity(inte)
//                            miAdaptadorRecycler.notifyDataSetChanged()
                        }
                    }))
                    setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->

                    }))
                    show()
                }
            }


            itemView.setOnLongClickListener() {
                with(builder)
                {
                    setTitle("Borrado")
                    setMessage("Esto borrará la joya de la base de datos, ¿Seguro que quieres continuar?")
                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                        runBlocking {
                            val trabajo : Job = launch(context = Dispatchers.Default) {
                                Store.JewelsCatalog.jewelsList.remove(jew)
                                FireStore.deleteJewelByName(jew.name)
                                if (jew.picture != Routes.defaultJewelPictureName){
                                    FireStore.deleteImageFromStorage(jew.picture!!,Routes.jewelsPicturesPath)
                                }
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


            //Se levanta una escucha para cada item. Si pulsamos el seleccionado pondremos la selección a -1, en otro caso será el nuevo sleccionado.
            itemView.setOnClickListener {
                InterWindows.iwJewel = Store.JewelsCatalog.jewelsList[pos] // valor dado por indice de pos en itemView desde ArrayList en Interventana

                if (InterWindows.iwJewel != null){

                    Log.d("JewelSelected", InterWindows.iwJewel.picture.toString())
                    var inte: Intent = Intent(context, AddJewel_Controller::class.java)
                    context.startActivity(inte)
                }
            }


        }
//        fun fileDownload(identificador: String?) {
//            var spaceRef = storageRef.child(Routes.jewelsPicturesPath + identificador)
//            val localfile = File.createTempFile(identificador!!, "jpeg")
//            spaceRef.getFile(localfile).addOnSuccessListener {
//                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
//                userPicture.setImageBitmap(bitmap)
//
//            }.addOnFailureListener {
//
//            }
//        }

        fun fileDownload(identificador: String?) {
            var spaceRef = storageRef.child(Routes.jewelsPicturesPath + identificador)
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


        suspend fun checkIfDoJewelIsPosible(jewel: Jewel): Boolean {
            var isPosible = false
            var actualInvertoryItems = FireStore.getItemsInventory()
            var quantityOfItems = 0

            for (component in actualInvertoryItems.sumarize) {
                for (componentJewel in jewel.components) {
                    if (componentJewel.name == component.name) {
                        if (component.quantity >= componentJewel.quantity) {
                            isPosible = true
                            quantityOfItems++
                        } else {
                            isPosible = false
                            break
                        }
                    }
                }
            }
            if(quantityOfItems != jewel.components.size) isPosible = false
            return isPosible
        }


    }// End of class ViewHolder


}// End of class RecyAdapterJeweler
