package Adapters


import Auxiliaries.InterWindows
import Auxiliaries.ObjectQuantity
import Connections.FireStore
import Constants.Routes

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.jawaschallenge.R
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import Model.Users.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecyAdapterInventory(var itemsInside : MutableList<ObjectQuantity>, var  context: Context) : RecyclerView.Adapter<RecyAdapterInventory.ViewHolder>() {


    var onItemClickListener: ViewHolder.OnItemClickListener? = null


    /**
     * onBindViewHolder() se encarga de coger cada una de las posiciones de la lista de personajes y pasarlas a la clase
     * ViewHolder(clase interna, ver abajo) para que esta pinte todos los valores y active el evento onClick en cada uno.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemsInside.get(position)
        holder.bind(item, context, position, this)

    }

    /**
     *  Como su nombre indica lo que hará será devolvernos un objeto ViewHolder al cual le pasamos la celda que hemos creado.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_card_itemstxt, parent, false)
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
        return itemsInside.size
    }

    //--------------------------------- Clase interna ViewHolder -----------------------------------
    /**
     * La clase ViewHolder. No es necesaria hacerla dentro del adapter, pero como van tan ligadas
     * se puede declarar aquí.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemInfo = view.findViewById(R.id.txtInfo) as TextView
        val storage = Firebase.storage
        val storageRef = storage.reference


        /**
         * Éste método se llama desde el método onBindViewHolder de la clase contenedora. Como no vuelve a crear un objeto
         * sino que usa el ya creado en onCreateViewHolder, las asociaciones findViewById no vuelven a hacerse y es más eficiente.
         */
        @SuppressLint("ResourceAsColor")
        fun bind(
            iteYewInfo: ObjectQuantity,
            context: Context,
            pos: Int,
            miAdaptadorRecycler: RecyAdapterInventory
        ) {
            val builder = AlertDialog.Builder(context)
            itemInfo.text = iteYewInfo.name + "    -    " + iteYewInfo.quantity //item type name


            val positiveButtonClick = { dialog: DialogInterface, which: Int ->
                Toast.makeText(context,
                    "Has pulsado sí", Toast.LENGTH_SHORT).show()
            }

            itemView.setOnLongClickListener() {
////                InterWindows.iwItem = InterWindows.iwItemsInside[pos]
//                InterWindows.iwItemToJewel = InterWindows.iwJewel.components[pos]
//                with(builder)
//                {
//                    setTitle("Estas a punto de borrar un item añadido a esta joya")
//                    setMessage("¿Seguro que quieres continuar?")
//                    setPositiveButton("Yes", android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
//
//                        runBlocking {
//                            val trabajo : Job = launch(context = Dispatchers.Default) {
//
//                                InterWindows.iwJewel.components.remove(iteYewInfo)
//
//                            }
//                            trabajo.join()
//                            miAdaptadorRecycler.notifyDataSetChanged()
//                        }
//                    }))
//                    setNegativeButton("No", ({ dialog: DialogInterface, which: Int ->
////                                Toast.makeText(context, "Has pulsado no", Toast.LENGTH_SHORT).show()
//                    }))
//                    show()
//                }
                true
            }


            //Se levanta una escucha para cada item. Si pulsamos el seleccionado pondremos la selección a -1, en otro caso será el nuevo sleccionado.
            itemView.setOnClickListener {

                InterWindows.iwItemInventory = InterWindows.inventory.sumarize[pos]
                Toast.makeText(context, "Has pulsado el item: " + InterWindows.iwItemInventory.name, Toast.LENGTH_SHORT).show()
//                itemView.setBackgroundColor(R.color.green)
            }

        }

        interface OnItemClickListener {
            fun onItemClick(view: View, position: Int)
        }


    }// End of class ViewHolder

}// End of class RecyAdapterItemsTxt
