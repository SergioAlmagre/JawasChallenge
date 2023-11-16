package Connections

import Auxiliaries.ObjectQuantity
import Auxiliaries.QuantitiesSumarize
import Model.Hardware.Batch
import Model.Hardware.BatchInfo
import Model.Hardware.Item
import Model.Jewels.Jewel
import Model.Users.Donor
import Model.Users.User
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object FireStore {
    val db = Firebase.firestore

    //    ------------------------- REGISTERS ------------------------------- //

    fun registerUser(user: User): Int {
        var cant = 0
        var us = hashMapOf(
            "name" to user.name,
            "email" to user.email.uppercase(),
            "address" to user.address,
            "phone" to user.phone,
            "picture" to user.picture,
            "role" to user.role
        )
        //if does exist the document, it will be replaced.
        db.collection("users")
            .document(us.get("email").toString()) //It will be "document key".
            .set(us).addOnSuccessListener {
                cant = 1
            }
        return cant
    }

    fun registerDonor(donor: Donor): Int {
        var cant = 0
        var us = hashMapOf(
            "name" to donor.name,
            "email" to donor.email.uppercase(),
            "address" to donor.address,
            "phone" to donor.phone,
            "picture" to donor.picture,
            "role" to donor.role,
            "batches" to donor.batches
        )
        //if does exist the document, it will be replaced.
        db.collection("users")
            .document(us.get("email").toString()) //It will be "document key".
            .set(us).addOnSuccessListener {
                cant = 1
            }
        return cant
    }

    fun registerBatch(batch: Batch): Int {
        var cant = 0
        var ba = hashMapOf(
            "idBatch" to batch.idBatch,
            "userName" to batch.userName.uppercase(),
            "latitude" to batch.latitude,
            "longitude" to batch.longitude,
            "address" to batch.address,
            "creationDate" to batch.creationDate,
            "delivered" to batch.received,
            "picture" to batch.picture,
            "isClassifed" to batch.isClassifed,
            "itemsInside" to batch.itemsInside
        )
        //if does exist the document, it will be replaced.
        db.collection("batches")
            .document(ba.get("idBatch").toString()) //It will be "document key".
            .set(ba).addOnSuccessListener {
                cant = 1
            }
        return cant
    }

    fun registerJewel(jewel: Jewel): Int {
        var cant = 0
        var je = hashMapOf(
            "name" to jewel.name.uppercase(),
            "instructions" to jewel.instructions,
            "price" to jewel.price,
            "picture" to jewel.picture,
            "components" to jewel.components,
        )
        db.collection("jewels")
            .document(je.get("name").toString())
            .set(je).addOnSuccessListener {
                cant = 1
            }
        return cant
    }


    fun registerNewItem(item: Item):Int{
        var cant = 0
        var it = hashMapOf(
            "itemId" to item.idItem,
            "attributes" to item.attributes,
            )
        db.collection("items")
            .document(it.get("itemId").toString())
            .set(it).addOnSuccessListener {
                cant = 1
            }
        return cant
    }

    //    ------------------------- ADD ITEM TO BATCH ------------------------------- //
    suspend fun addItemToBatch(userEmail: String, batchId: Int, item: Item) {
        // Referencia al documento del cliente
        val clientDocument = db.collection("users").document(userEmail)

        try {
            // Obtener el cliente
            val client = clientDocument.get().await().toObject(Donor::class.java)

            // Verificar si el objeto Cliente es válido y si tiene batches
            if (client != null && client.batches.isNotEmpty()) {
                for (batch in client.batches) {
                    // Verificar si el batch ha sido recibido pero no clasificado
                    if (batch.received && !batch.isClassifed && batch.idBatch == batchId) {
                        // Agregar el ítem al batch
                        batch.itemsInside.add(item)
                    }
                }

                // Actualizar el documento del cliente con los batches modificados
                clientDocument.update("batches", client.batches)
                    .addOnSuccessListener {
                        Log.d("addItemToBatch", "Batches actualizados con éxito")
                    }
                    .addOnFailureListener { e ->
                        Log.e("addItemToBatch", "Error al actualizar batches: $e")
                    }
            }
        } catch (exception: Exception) {
            println("Error al obtener datos: $exception")
            Log.e("addItemToBatch", "Error al obtener datos: $exception")
        }
    }



    //    ------------------------- GET DATA ------------------------------- //
    suspend fun getAllPendingBatchesFromUsers() { // THIS QUERY SHOW US ONLY THE BATCHES THAT ARE NOT CLASSIFIED
        // Referencia a la colección de usuarios
        val usersCollection = db.collection("users")

        // Lista para almacenar todos los lotes
        val allBatches = mutableListOf<Batch>()

        try {
            val querySnapshot = usersCollection.get().await()

            for (document in querySnapshot.documents) {
                // Obtener el usuario y su lista de lotes
                val user = document.toObject(Donor::class.java)

                // Verificar si el objeto User es válido y tiene lotes
                if (user != null) {
                    // Filtrar los lotes no clasificados
                    val unclassifiedBatches = user.batches.filter { !it.isClassifed }

                    // Agregar los lotes no clasificados a la lista
                    allBatches.addAll(unclassifiedBatches)
                }
            }

            // Ordenar los lotes por fecha de creación
            val sortedBatches = allBatches.sortedBy { it.creationDate }

            // Actualizar la lista en el objeto Store.BatchesStore
            Store.PendingBatches.batchList = sortedBatches.toMutableList()
        } catch (exception: Exception) {
            Log.d("Batches", "Error al obtener datos: $exception")
            println("Error al obtener datos: $exception")
        }
    }

    suspend fun getAllItems() {
        // Referencia a la colección de items
        val itemsCollection = db.collection("items")

        // Lista para almacenar todos los items
        val allItems = mutableListOf<Item>()

        try {
            val querySnapshot = itemsCollection.get().await()

            for (document in querySnapshot.documents) {
                // Obtener el item
                val item = document.toObject(Item::class.java)

                // Verificar si el objeto Item es válido
                if (item != null) {
                    allItems.add(item)
                }
            }

            // Ordenar los items por el atributo "Type" alfabéticamente
            val sortedItems = allItems.sortedBy { it.attributes.find { attr -> attr.name == "Type" }?.content }

            // Actualizar la lista en el objeto Store.ItemsStore
            Store.ItemsStore.itemsList = sortedItems.toMutableList()
        } catch (exception: Exception) {
            Log.d("Items", "Error al obtener datos: $exception")
            println("Error al obtener datos: $exception")
        }
    }


    suspend fun getBatchInfoById(userEmail: String, batchId: Int): BatchInfo? {
        try {
            // Referencia al documento del usuario
            val userDocument = db.collection("users").document(userEmail)

            // Obtener el usuario
            val user = userDocument.get().await().toObject(Donor::class.java)

            // Verificar si el objeto Usuario es válido y si tiene lotes
            if (user != null && user.batches.isNotEmpty()) {
                // Buscar el lote con el idBatch específico
                val batch = user.batches.find { it.idBatch == batchId }

                // Si se encuentra el lote, crear un objeto BatchInfo con la información
                if (batch != null) {
                    return BatchInfo(
                        batchID = batch.idBatch,
                        userName = user.name,
                        email = user.email,
                        address = user.address ?: "",
                        creationDate = batch.creationDate,
                        isReceived = batch.received
                    )
                }
            }
        } catch (exception: Exception) {
            println("Error al obtener datos: $exception")
        }

        return null // Retorna null si no se encuentra el lote con el idBatch específico
    }








    //    ------------------------- COUNT QUANTITIES OF COMPONENTS------------------------------- //

    suspend fun getCountTypesOfItem(): QuantitiesSumarize {
        // Referencia a la colección
        val itemsCollection = db.collection("items")

        // Mapa para almacenar los resultados
        val countsMap = mutableMapOf<String, Long>()

        try {
            val querySnapshot = itemsCollection.get().await()

            for (document in querySnapshot.documents) {
                val item = document.toObject(Item::class.java)

                // Verificar si el objeto Item es válido y tiene componentes
                if (item != null) {
                    for (attribute in item.attributes) {
                        if (attribute.name == "Type" && attribute.content != null) {
                            countsMap[attribute.content!!] = countsMap.getOrDefault(attribute.content!!, 0) + 1
                        }
                    }
                }
            }

            // Crear una lista de pares (tipo de componente, cantidad)
            val componentCountList = countsMap.entries.map { ObjectQuantity(it.key, it.value) }

            // Ordenar la lista de mayor a menor según la cantidad
            val sortedComponentCountList = componentCountList.sortedByDescending { it.quantity }

            // Ahora sortedComponentCountList contiene los elementos ordenados
            Log.e("Sergio", sortedComponentCountList.toString())
            return QuantitiesSumarize(sortedComponentCountList)
        } catch (exception: Exception) {
            println("Error al obtener datos: $exception")
            Log.e("Count", "Error al obtener datos: $exception")
            return QuantitiesSumarize(emptyList())
        }
    }





//    ------------------------- DIFFERENT TYPES OF ITEMS------------------------------- //

    suspend fun getAllDistinctTypes() {
        // Referencia a la colección
        val itemsCollection = db.collection("items")

        // Conjunto para almacenar los resultados únicos
        val typesSet = mutableSetOf<String>()

        try {
            val querySnapshot = itemsCollection.get().await()

            for (document in querySnapshot.documents) {
                val attributes = document.get("attributes") as? List<Map<String, Any>>

                // Buscar el atributo con nombre "Type"
                val typeAttribute = attributes?.find { it["name"] == "Type" }

                // Agregar el valor del atributo "Type" al conjunto
                val typeValue = typeAttribute?.get("content") as? String
                if (typeValue != null) {
                    typesSet.add(typeValue)
                }
            }

            // Send to Store all different types
            Store.Types.allTypesList = typesSet.sorted().toMutableList().toMutableList()
        } catch (exception: Exception) {
            println("Error al obtener datos: $exception")
        }
    }





    //    ------------------------- UPDATAES ------------------------------- //
    suspend fun updateDifferentsFromEach(){
        getAllDistinctTypes()
    }


//    ------------------------- GET SPECIFIC DATA ------------------------------- //










//    ------------------------- OPERATING FUNCTIONS ------------------------------- //
    suspend fun endBatch(userEmail: String, batchId: Int) {
        // Referencia al documento del cliente
        val clientDocument = db.collection("users").document(userEmail)

        try {
            // Obtener el cliente
            val client = clientDocument.get().await().toObject(Donor::class.java)

            // Verificar si el objeto Cliente es válido y si tiene batches
            if (client != null && client.batches.isNotEmpty()) {
                for (batch in client.batches) {
                    // Verificar si el batch ha sido recibido pero no clasificado
                    if (batch.received && !batch.isClassifed && batch.idBatch == batchId) {

                        // Agregar los ítems del batch a ItemsStore
                        batch.itemsInside.forEach { item ->
                            Store.ItemsStore.itemsList.add(item)
                        }
                        Log.d( "ItemsStore",Store.ItemsStore.itemsList.toString())
                        // Update ItemsStore and ItemsCollection in FireStore
                        updateItemsStore()

                        // Establecer isClassifed a true
                        batch.isClassifed = true
                    }
                }

                // Actualizar el documento del cliente con los batches modificados
                clientDocument.update("batches", client.batches)
                    .addOnSuccessListener {
                        Log.d("endBatch", "Batches actualizados con éxito")
                    }
                    .addOnFailureListener { e ->
                        Log.e("endBatch", "Error al actualizar batches: $e")
                    }
            }
        } catch (exception: Exception) {
            println("Error al obtener datos: $exception")
            Log.e("endBatch", "Error al obtener datos: $exception")
        }
    }

    fun updateItemsStore() {
        try {
            val items = Store.ItemsStore.itemsList

            // Iterar sobre los ítems en la ItemsStore
            for (item in items) {
                val itemId = item.idItem

                // Crear un mapa con los datos actualizados del ítem
                val updatedItemData = hashMapOf(
                    "attributes" to item.attributes,
                    // Agrega otros campos que necesites actualizar
                )

                // Actualizar o crear el documento del ítem en la colección "items"
                db.collection("items")
                    .document(itemId.toString())
                    .set(updatedItemData)
                    .addOnSuccessListener {
                        Log.d("updateItemsStore", "Ítem actualizado con éxito o creado: $itemId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("updateItemsStore", "Error al actualizar o crear ítem: $itemId, Error: $e")
                    }
            }
        } catch (exception: Exception) {
            Log.e("updateItemsStore", "Error al obtener datos: $exception")
        }
    }






}