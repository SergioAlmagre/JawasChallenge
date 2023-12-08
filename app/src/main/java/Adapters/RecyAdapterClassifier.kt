package Adapters


import Auxiliaries.InterWindows
import Constants.Routes
import Controllers.Classifier.ViewItemsBatch_Controller
import Controllers.Donor.BatchDetails_Controller
import Model.Hardware.Batch
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
import com.google.firebase.storage.storage
import java.io.File
import android.content.Intent
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.content.ContextCompat

class RecyAdapterClassifier(var batch : MutableList<Batch>, var  context: Context) : RecyclerView.Adapter<RecyAdapterClassifier.ViewHolder>() {

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
        val item = batch.get(position)
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
        return batch.size
    }

    //--------------------------------- Clase interna ViewHolder -----------------------------------
    /**
     * La clase ViewHolder. No es necesaria hacerla dentro del adapter, pero como van tan ligadas
     * se puede declarar aquí.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val creationDate = view.findViewById(R.id.txtInfo) as TextView
        val batchPicture = view.findViewById(R.id.ObjetPicture) as ImageView
        val checkRecibed = view.findViewById(R.id.colorLayoutReceived) as FrameLayout
        val playIcon = view.findViewById(R.id.btnBuildJewel) as ImageButton


        val storage = Firebase.storage
        val storageRef = storage.reference

        /**
         * Éste método se llama desde el método onBindViewHolder de la clase contenedora. Como no vuelve a crear un objeto
         * sino que usa el ya creado en onCreateViewHolder, las asociaciones findViewById no vuelven a hacerse y es más eficiente.
         */
        @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
        fun bind(
            bat: Batch,
            context: Context,
            pos: Int,
            miAdaptadorRecycler: RecyAdapterClassifier
        ) {
            val builder = AlertDialog.Builder(context)
            creationDate.text = bat.userName + " - " + bat.creationDate
            fileDownload(bat.picture)
            checkRecibed.visibility = View.VISIBLE
            playIcon.visibility = View.INVISIBLE


            val positiveButtonClick = { dialog: DialogInterface, which: Int ->
                Toast.makeText(context,
                    "Has pulsado sí", Toast.LENGTH_SHORT).show()
            }


            if(bat.received)
            {
                checkRecibed.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
            }else{
                checkRecibed.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            }

            itemView.setOnLongClickListener() {
                InterWindows.iwBatch = InterWindows.iwPendingBatches[pos]
                   if(!InterWindows.iwBatch.received){

                       val builder = AlertDialog.Builder(context)
                       with(builder)
                       {
                           setTitle("Ojo!")
                           setMessage("No puedes clasificar sin haber recibido el lote")
                           setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))
                           show()
                       }

                   }else{

                       var intent = Intent(context, ViewItemsBatch_Controller::class.java)
                       context.startActivity(intent)

                   }


                true
            }


            //Se levanta una escucha para cada item. Si pulsamos el seleccionado pondremos la selección a -1, en otro caso será el nuevo sleccionado.
            itemView.setOnClickListener {
                InterWindows.iwBatch = InterWindows.iwPendingBatches[pos] // valor dado por indice de pos en itemView desde ArrayList en Interventana

                if (InterWindows.iwBatch != null){
                    Toast.makeText(
                        context,
                        "Seleccionado " + InterWindows.iwBatch!!.creationDate,
                        Toast.LENGTH_SHORT
                    ).show()
                    var inte: Intent = Intent(context, BatchDetails_Controller::class.java)
                    context.startActivity(inte)
                }
            }


        }
        fun fileDownload(identificador: String?) {

            var spaceRef = storageRef.child(Routes.batchesPicturesPath + identificador)
            val localfile = File.createTempFile(identificador!!, "jpeg")
            spaceRef.getFile(localfile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                batchPicture.setImageBitmap(bitmap)
            }.addOnFailureListener {

            }
        }
    }


}
