package Connections

import Auxiliaries.ObjectQuantity
import Auxiliaries.QuantitiesSumarize
import Model.Hardware.Batch
import Model.Hardware.BatchInfo
import Model.Hardware.Item
import Model.Jewels.Jewel
import Model.Users.User
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object FireStore {
    val db = Firebase.firestore

    //    ------------------------- ADD ------------------------------- //

    fun addUser(user: User): Int {
        var cant = 0
        var us = hashMapOf(
            "name" to user.name,
            "email" to user.email.uppercase(),
            "address" to user.address,
            "phone" to user.phone,
            "picture" to user.picture,
            "role" to user.role,
            "batches" to user.batches
        )
        //if does exist the document, it will be replaced.
        db.collection("users")
            .document(us.get("email").toString()) //It will be "document key".
            .set(us).addOnSuccessListener {
                cant = 1
            }
        return cant
    }


    suspend fun addOrUpdateBatchToDonor(email: String, batch: Batch): Int {
        var cant = 0

        // Obtener la referencia al documento del donante
        val donorDocument = db.collection("users").document(email)

        try {
            // Obtener el documento del donante
            val donor = donorDocument.get().await().toObject(User::class.java)

            // Verificar si el objeto Donor es válido
            if (donor != null) {
                // Buscar el índice del lote con el mismo idBatch en la lista de lotes
                val existingBatchIndex = donor.batches.indexOfFirst { it.idBatch == batch.idBatch }

                if (existingBatchIndex != -1) {
                    // Si existe, actualizar el lote en la lista
                    donor.batches[existingBatchIndex] = batch
                } else {
                    // Si no existe, agregar el nuevo lote a la lista
                    donor.batches.add(batch)
                }

                // Actualizar el documento del donante con la lista de lotes modificada
                donorDocument.update("batches", donor.batches)
                    .addOnSuccessListener {
                        cant = 1
                        Log.d("addOrUpdateBatchToDonor", "Lote agregado o actualizado con éxito")
                    }
                    .addOnFailureListener { e ->
                        Log.e("addOrUpdateBatchToDonor", "Error al agregar o actualizar lote: $e")
                    }
            }
        } catch (exception: Exception) {
            Log.e("addOrUpdateBatchToDonor", "Error al obtener datos: $exception")
        }

        return cant
    }


    fun addJewelToCatalog(jewel: Jewel): Int {
        var cant = 0
        var je = hashMapOf(
            "name" to jewel.name.uppercase(),
            "instructions" to jewel.instructions,
            "price" to jewel.price,
            "picture" to jewel.picture,
            "components" to jewel.components,
        )
        db.collection("jewelsCatalog")
            .document(je.get("name").toString())
            .set(je).addOnSuccessListener {
                cant = 1
            }
        return cant
    }


    fun addNewItem(item: Item): Int {
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


    suspend fun addItemToBatch(userEmail: String, batchId: String, item: Item) {
        // Referencia al documento del cliente
        val clientDocument = db.collection("users").document(userEmail)

        try {
            // Obtener el cliente
            val client = clientDocument.get().await().toObject(User::class.java)

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


    suspend fun addNewTypeToFirebase(typeName: String): Boolean {
        val typesCollection = db.collection("itemsTypes")

        val newType = hashMapOf(
            "typeName" to typeName
        )

        try {
            typesCollection
                .document(typeName)
                .set(newType)
                .await()

            return true
        } catch (exception: Exception) {
            println("Error al añadir nuevo tipo a Firebase: $exception")
            return false
        }
    }


    //    ---------------------------- GET --------------------------------- //



    suspend fun getUserByEmail(email: String): User? {

        try {
            val documentSnapshot = db.collection("users").document(email).get().await()

            if (documentSnapshot.exists()) {
                // El documento existe, devuelve el usuario
                return documentSnapshot.toObject(User::class.java)
            } else {
                // El documento no existe
                return null
            }
        } catch (e: Exception) {
            // Maneja cualquier excepción que pueda ocurrir durante la obtención de datos
            return null
        }
    }



    suspend fun getAllPendingBatchesFromUsers() { // THIS QUERY SHOW US ONLY THE BATCHES THAT ARE NOT CLASSIFIED
        // Referencia a la colección de usuarios
        val usersCollection = db.collection("users")

        // Lista para almacenar todos los lotes
        val allBatches = mutableListOf<Batch>()

        try {
            val querySnapshot = usersCollection.get().await()

            for (document in querySnapshot.documents) {
                // Obtener el usuario y su lista de lotes
                val user = document.toObject(User::class.java)

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
            val sortedItems =
                allItems.sortedBy { it.attributes.find { attr -> attr.name == "Type" }?.content }

            // Actualizar la lista en el objeto Store.ItemsStore
            Store.ItemsStore.itemsList = sortedItems.toMutableList()
        } catch (exception: Exception) {
            Log.d("Items", "Error al obtener datos: $exception")
            println("Error al obtener datos: $exception")
        }
    }


    suspend fun getBatchInfoById(userEmail: String, batchId: String): BatchInfo? {
        try {
            // Referencia al documento del usuario
            val userDocument = db.collection("users").document(userEmail)

            // Obtener el usuario
            val user = userDocument.get().await().toObject(User::class.java)

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

    suspend fun getAllJewels() {
        // Referencia a la colección de joyas
        val jewelsCollection = db.collection("jewelsCatalog")

        // Lista para almacenar todas las joyas
        val allJewels = mutableListOf<Jewel>()

        try {
            val querySnapshot = jewelsCollection.get().await()

            for (document in querySnapshot.documents) {
                // Obtener la joya
                val jewel = document.toObject(Jewel::class.java)

                // Verificar si el objeto Jewel es válido
                if (jewel != null) {
                    allJewels.add(jewel)
                }
            }

            // Ordenar las joyas por nombre alfabéticamente
            val sortedJewels = allJewels.sortedBy { it.name }
            Log.d("getAllJewels", sortedJewels.toString())

            // Actualizar la lista en el objeto Store.JewelsStore
            Store.JewelsCatalog.jewelsList = sortedJewels.toMutableList()
        } catch (exception: Exception) {
            Log.d("Jewels", "Error al obtener datos: $exception")
            println("Error al obtener datos: $exception")
        }
    }

//    suspend fun getAllDistinctTypes() { // De esta forma recopilaba los tipos desde los items
//        // Referencia a la colección
//        val itemsCollection = db.collection("items")
//
//        // Conjunto para almacenar los resultados únicos
//        val typesSet = mutableSetOf<String>()
//
//        try {
//            val querySnapshot = itemsCollection.get().await()
//
//            for (document in querySnapshot.documents) {
//                val attributes = document.get("attributes") as? List<Map<String, Any>>
//
//                // Buscar el atributo con nombre "Type"
//                val typeAttribute = attributes?.find { it["name"] == "Type" }
//
//                // Agregar el valor del atributo "Type" al conjunto
//                val typeValue = typeAttribute?.get("content") as? String
//                if (typeValue != null) {
//                    typesSet.add(typeValue)
//                }
//            }
//
//            // Conservar los tipos antiguos
//            val oldTypes = Store.ItemsTypes.allTypesList
//
//            // Agregar los tipos antiguos al conjunto
//            typesSet.addAll(oldTypes)
//
//            // Actualizar la lista en Store.itemsTypes
//            Store.ItemsTypes.allTypesList = typesSet.sorted().toMutableList()
//
//        } catch (exception: Exception) {
//            println("Error al obtener datos: $exception")
//        }
//    }


    suspend fun getAllDistinctTypes() {
        // Referencia a la colección
        val typesCollection = db.collection("itemsTypes")

        // Lista para almacenar los resultados
        val typesList = mutableListOf<String>()

        try {
            val querySnapshot = typesCollection.get().await()

            for (document in querySnapshot.documents) {
                val type = document.getString("typeName")
                type?.let { typesList.add(it) }
            }

            // Actualizar la lista en Store.itemsTypes
            Store.ItemsTypes.allTypesList = typesList.sorted().toMutableList()
        } catch (exception: Exception) {
            println("Error al obtener datos: $exception")
        }
    }


    suspend fun getItemsInventory(): QuantitiesSumarize {
        // Referencia a la colección
        val itemsCollection = db.collection("items")

        // Mapa para almacenar los resultados
        val countsMap = mutableMapOf<String, Int>()

        try {
            val querySnapshot = itemsCollection.get().await()

            for (document in querySnapshot.documents) {
                val item = document.toObject(Item::class.java)

                // Verificar si el objeto Item es válido y tiene componentes
                if (item != null) {
                    for (attribute in item.attributes) {
                        if (attribute.name == "Type" && attribute.content != null) {
                            countsMap[attribute.content!!] =
                                countsMap.getOrDefault(attribute.content!!, 0) + 1
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


    suspend fun getJewelByName(jewelName: String): Jewel? {
        try {
            // Referencia a la colección de joyas
            val jewelsCollection = db.collection("jewelsCatalog")

            // Realizar una consulta para obtener la joya por nombre
            val querySnapshot = jewelsCollection.whereEqualTo("name", jewelName).get().await()

            // Verificar si se encontró alguna joya
            if (!querySnapshot.isEmpty) {
                // Obtener el primer documento (debería ser único por nombre)
                val jewelDocument = querySnapshot.documents[0]

                // Convertir el documento a un objeto Jewel
                return jewelDocument.toObject(Jewel::class.java)
            } else {
                // No se encontró ninguna joya con el nombre proporcionado
                Log.d("getJewelByName", "No se encontró ninguna joya con el nombre: $jewelName")
            }
        } catch (exception: Exception) {
            Log.e("getJewelByName", "Error al obtener la joya: $exception")
        }

        // En caso de error o si no se encuentra ninguna joya, devolver null
        return null
    }


    //    ------------------------- OPERATING FUNCTIONS ------------------------------- //
    suspend fun endBatch(userEmail: String, batchId: String) {
        // Referencia al documento del cliente
        val clientDocument = db.collection("users").document(userEmail)

        try {
            // Obtener el cliente
            val client = clientDocument.get().await().toObject(User::class.java)
            Log.d("cliente", client.toString())

            // Verificar si el objeto Cliente es válido y si tiene batches
            if (client != null && client.batches.isNotEmpty()) {
                for (batch in client.batches) {
                    // Verificar si el batch ha sido recibido pero no clasificado
                    if (batch.received && !batch.isClassifed && batch.idBatch == batchId) {

                        // Agregar los ítems del batch a ItemsStore
                        batch.itemsInside.forEach { item ->
                            Store.ItemsStore.itemsList.add(item)
                        }
                        Log.d("ItemsStore", Store.ItemsStore.itemsList.toString())
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


    //    ------------------------- UPDATES ------------------------------- //

    suspend fun chargeDataBase() {
        updateItemsStore()
    }


    suspend fun updateLocalTypesFromFirebase(): List<String> {
        val typesCollection = db.collection("ItemsTypes")

        try {
            val querySnapshot = typesCollection.get().await()

            val typesList = mutableListOf<String>()

            for (document in querySnapshot.documents) {
                val type = document.getString("typeName")
                type?.let { typesList.add(it) }
            }

            // Actualizar la lista local en Store.itemsTypes
            Store.ItemsTypes.allTypesList.addAll(typesList)

            return typesList
        } catch (exception: Exception) {
            println("Error al obtener tipos desde Firebase: $exception")
            return emptyList()
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
                    "itemId" to item.idItem,
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
                        Log.e(
                            "updateItemsStore",
                            "Error al actualizar o crear ítem: $itemId, Error: $e"
                        )
                    }
            }
        } catch (exception: Exception) {
            Log.e("updateItemsStore", "Error al obtener datos: $exception")
        }
    }


    suspend fun updateUserRoleByEmail(userEmail: String, newRole: String) {
        try {
            // Referencia al documento del usuario en la colección "users"
            val userDocument = db.collection("users").document(userEmail)

            // Actualizar el rol del usuario en el documento
            userDocument.update("role", newRole).await()

            Log.d("updateUserRoleByEmail", "Rol del usuario actualizado con éxito: $userEmail")
        } catch (exception: Exception) {
            Log.e("updateUserRoleByEmail", "Error al actualizar rol del usuario: $exception")
        }
    }





    //    ------------------------- DELETE ------------------------------- //

    suspend fun deleteItemsForJewel(jewel: Jewel) {
        try {
            // Referencia a la colección de items
            val itemsCollection = db.collection("items")

            // Recorrer los componentes de la joya
            for (component in jewel.components) {
                val componentName = component.name
                val componentQuantity = component.quantity

                // Realizar una consulta para obtener los items del tipo de componente
                val querySnapshot = itemsCollection
                    .whereArrayContains("attributes", mapOf("name" to "Type", "content" to componentName))
                    .limit(componentQuantity.toLong())
                    .get()
                    .await()

                // Eliminar los items encontrados
                for (document in querySnapshot.documents) {
                    val itemId = document.id
                    itemsCollection.document(itemId).delete().await()
                    Log.d("deleteItemsForJewel", "Ítem eliminado con éxito: $itemId")
                }

                Log.d("deleteItemsForJewel", "Componente: $componentName, Cantidad eliminada: ${querySnapshot.size()}")
            }

        } catch (exception: Exception) {
            Log.e("deleteItemsForJewel", "Error al eliminar ítems: $exception")
        }
    }


    suspend fun deleteJewelByName(jewelName: String) {
        try {
            // Referencia al documento de la joya en el catálogo
            val jewelDocument = db.collection("jewelsCatalog").document(jewelName)

            // Borrar el documento de la joya
            jewelDocument.delete().await()

            // También puedes agregar aquí cualquier otra lógica que necesites después de borrar la joya
            Log.d("deleteJewelByName", "Joya eliminada con éxito: $jewelName")
        } catch (exception: Exception) {
            Log.e("deleteJewelByName", "Error al eliminar joya: $exception")
        }
    }


    suspend fun deleteItemsTypeByName(itemTypeName: String) {
        try {
            // Referencia al documento del tipo de ítem en la colección "ItemsTypes"
            val itemTypeDocument = db.collection("itemsTypes").document(itemTypeName)

            // Borrar el documento del tipo de ítem
            itemTypeDocument.delete().await()

            // También puedes agregar aquí cualquier otra lógica que necesites después de borrar el tipo de ítem
            Log.d("deleteItemsTypeByName", "Tipo de ítem eliminado con éxito: $itemTypeName")
        } catch (exception: Exception) {
            Log.e("deleteItemsTypeByName", "Error al eliminar tipo de ítem: $exception")
        }
    }


    suspend fun deleteItemById(itemId: String) {
        try {
            // Referencia al documento del ítem en la colección "items"
            val itemDocument = db.collection("items").document(itemId)

            // Borrar el documento del ítem
            itemDocument.delete().await()

            // También puedes agregar aquí cualquier otra lógica que necesites después de borrar el ítem
            Log.d("deleteItemById", "Ítem eliminado con éxito: $itemId")
        } catch (exception: Exception) {
            Log.e("deleteItemById", "Error al eliminar ítem: $exception")
        }
    }




    suspend fun deleteUserByEmail(email: String) {
        try {
            // Referencia al documento del usuario
            val userDocument = db.collection("users").document(email)

            // Borrar el documento del usuario
            userDocument.delete().await()

            // También puedes agregar aquí cualquier otra lógica que necesites después de borrar el usuario
            Log.d("deleteUserByEmail", "Usuario eliminado con éxito: $email")
        } catch (exception: Exception) {
            Log.e("deleteUserByEmail", "Error al eliminar usuario: $exception")
        }
    }
















    //    ------------------------- TO CHECK THINGS ------------------------------- //


    suspend fun addItemToFireStore(item: Item){
        var it = hashMapOf(
            "itemId" to item.idItem,
            "attributes" to item.attributes,
        )
        db.collection("items")
            .document(it.get("itemId").toString())
            .set(it).addOnSuccessListener {
                Log.d("addItemToFireStore", "Item agregado con éxito")
            }
            .addOnFailureListener { e ->
                Log.e("addItemToFireStore", "Error al agregar item: $e")
            }
    }


}

