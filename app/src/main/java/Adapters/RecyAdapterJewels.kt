package Adapters


import Auxiliaries.InterWindows
import Connections.FireStore
import Controllers.UserDetailsAdmin_Controller
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
import Store.ItemsStore
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecyAdapterJewels(var jewels : MutableList<Jewel>, var  context: Context) : RecyclerView.Adapter<RecyAdapterJewels.ViewHolder>() {

    companion object {
        //Esta variable estática nos será muy útil para saber cual está marcado o no.
        var seleccionado: Int = -1
        /*
        PAra marcar o desmarcar un elemento de la lista lo haremos diferente a una listView. En la listView el listener
        está en la activity por lo que podemos controlar desde fuera el valor de seleccionado y pasarlo al adapter, asociamos
        el adapter a la listview y resuelto.
        En las RecyclerView usamos para pintar cada elemento la función bind (ver código más abajo, en la clase ViewHolder).
        Esto se carga una vez, solo una vez, de ahí la eficiencia de las RecyclerView. Si queremos que el click que hagamos
        se vea reflejado debemos recargar la lista, para ello forzamos la recarga con el método: notifyDataSetChanged().
         */
    }

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
//        val layoutInflater = LayoutInflater.from(parent.context)
//        //return ViewHolder(layoutInflater.inflate(R.layout.item_lo,parent,false))
//        return ViewHolder(layoutInflater.inflate(R.layout.item_card,parent,false))
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_card_users, parent, false)
        val viewHolder = ViewHolder(vista)

        // Configurar el OnClickListener
        viewHolder.itemView.setOnClickListener {
//            val intent = Intent(context, VentanaLista::class.java)
//            context.startActivity(intent)
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

        val storage = Firebase.storage
        val storageRef = storage.reference
        val filePath = "JewelsPictures/"

        /**
         * Éste método se llama desde el método onBindViewHolder de la clase contenedora. Como no vuelve a crear un objeto
         * sino que usa el ya creado en onCreateViewHolder, las asociaciones findViewById no vuelven a hacerse y es más eficiente.
         */
        @SuppressLint("ResourceAsColor")
        fun bind(
            jew: Jewel,
            context: Context,

            pos: Int,
            miAdaptadorRecycler: RecyAdapterJewels
        ) {
            val builder = AlertDialog.Builder(context)
            mailUser.text = jew.name
            fileDownload(jew.picture)

            itemView.setOnLongClickListener() {

                with(builder)
                {
                    setTitle("Delete")
                    setMessage("This will delete the user. Are you sure?")
                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->

                        runBlocking {
                            val trabajo : Job = launch(context = Dispatchers.Default) {
//                                FireStore.deleteUserByEmail(jew.email) // Borrar usuario
//                                FireStore.deleteUserPicture(jew.email) // Borrar foto de perfil

                                Store.JewelsCatalog.jewelsList.remove(jew)
                                FireStore.deleteJewelByName(jew.name)
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
                InterWindows.iwUser = InterWindows.iwUsersAL[pos] // valor dado por indice de pos en itemView desde ArrayList en Interventana

                if (InterWindows.iwUser != null){
                    Toast.makeText(
                        context,
                        "Seleccionado " + InterWindows.iwUser!!.email,
                        Toast.LENGTH_SHORT
                    ).show()
                    var inte: Intent = Intent(context, UserDetailsAdmin_Controller::class.java)
                    context.startActivity(inte)
                }
            }


        }
        fun fileDownload(identificador: String?) {

            var spaceRef = storageRef.child(filePath + identificador)
            val localfile = File.createTempFile(identificador!!, "jpeg")
            spaceRef.getFile(localfile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                userPicture.setImageBitmap(bitmap)
            }.addOnFailureListener {

            }
        }
    }


}
