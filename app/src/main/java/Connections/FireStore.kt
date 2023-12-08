package Connections

import Auxiliaries.Attribute
import Auxiliaries.InterWindows
import Auxiliaries.ObjectQuantity
import Auxiliaries.QuantitiesSumarize
import Model.Hardware.Batch
import Model.Hardware.BatchInfo
import Model.Hardware.Item
import Model.Jewels.Jewel
import Model.Users.User
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

object FireStore {
    val db = Firebase.firestore

    //    ------------------------- ADD ------------------------------- //

    fun addUser(user: Model.Users.User): Int {
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



    //    ---------------------------- GET --------------------------------- //

    suspend fun getAllUsers(): ArrayList<Model.Users.User> {

        try {
            val querySnapshot = db.collection("users").get().await()

            // Mapea los documentos a objetos User y devuelve la lista
            return ArrayList(querySnapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            })
        } catch (e: Exception) {
            // Maneja cualquier excepción que pueda ocurrir durante la obtención de datos
            return ArrayList()
        }
    }


    suspend fun getUserByEmail(email: String): Model.Users.User? {
        try {
            val documentSnapshot = db.collection("users").document(email).get().await()

            if (documentSnapshot.exists()) {
                return documentSnapshot.toObject(User::class.java)
            } else {
                return null
            }
        } catch (e: Exception) {
            // Loguea detalles específicos de la excepción para depurar
            Log.e("getUserByEmail", "Error al obtener usuario por correo electrónico: ${e.message}")
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
            InterWindows.iwPendingBatches = sortedBatches.toMutableList()
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


    suspend fun getItemsForBatchReceived(batchId: String): MutableList<Item> {
        val itemsList = mutableListOf<Item>()

        try {
            // Obtén una referencia a la colección de usuarios
            val usersCollection = db.collection("users")

            // Realiza una consulta para obtener todos los documentos de la colección
            val querySnapshot = usersCollection.get().await()

            // Itera sobre los documentos de la colección de usuarios
            for (userDocument in querySnapshot.documents) {
                // Verifica si el documento del usuario existe y contiene la lista de batches
                if (userDocument.exists()) {
                    // Obtén la lista de batches del usuario
                    val batchesData = userDocument.get("batches") as? List<Map<String, Any>>

                    // Verifica que la lista de batches no sea nula
                    if (batchesData != null) {
                        // Filtra los batches con "received" igual a true
                        val receivedBatches = batchesData.filter { it["received"] == true }

                        // Busca el batch con el ID deseado en la lista de batches recibidos
                        val targetBatch = receivedBatches.find { it["idBatch"] == batchId }

                        // Verifica que el batch con el ID deseado existe
                        if (targetBatch != null) {
                            // Obtén la lista de itemsInside del batch
                            val itemsInsideData = targetBatch["itemsInside"] as? List<Map<String, Any>>

                            // Verifica que la lista de itemsInside no sea nula
                            if (itemsInsideData != null) {
                                // Itera sobre cada item y convierte los datos a objetos Item
                                for (itemData in itemsInsideData) {
                                    val attributesData = itemData["attributes"] as? List<Map<String, String>>
                                    val attributes = attributesData?.map { attributeData ->
                                        Attribute(
                                            name = attributeData["name"] ?: "",
                                            content = attributeData["content"] ?: ""
                                        )
                                    }?.toMutableList() ?: mutableListOf()

                                    val item = Item().apply {
                                        // Configura los atributos del Item según los datos de Firebase
                                        idItem = itemData["idItem"] as? String ?: ""
                                        this.attributes = attributes
                                    }

                                    itemsList.add(item)
                                    Log.d("getItemsForBatchReceived", "Item agregado: $item")
                                }
                            }
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            // Maneja las excepciones aquí
            Log.e("getItemsForBatchReceived", "Error al obtener items: $exception")
        }

        return itemsList
    }










    suspend fun getBatchesForUser(userId: String): MutableList<Batch> {
        val batchesList = mutableListOf<Batch>()

        try {
            // Obtén una referencia a la colección de usuarios
            val usersCollection = db.collection("users")

            // Realiza una consulta para obtener el documento del usuario
            val userDocument = usersCollection.document(userId).get().await()

            // Verifica si el documento del usuario existe y contiene la lista de lotes
            if (userDocument.exists()) {
                val batchesData = userDocument.get("batches") as? List<Map<String, Any>>

                // Verifica que la lista de lotes no sea nula
                if (batchesData != null) {
                    // Itera sobre cada lote y convierte los datos a objetos Batch
                    for (batchData in batchesData) {
                        val batch = Batch().apply {
                            idBatch = batchData["idBatch"] as? String ?: ""
                            userName = batchData["userName"] as? String ?: ""
                            latitude = batchData["latitude"] as? Double
                            longitude = batchData["longitude"] as? Double
                            address = batchData["address"] as? String
                            creationDate = batchData["creationDate"] as? String ?: ""
                            received = batchData["received"] as? Boolean ?: false
                            picture = batchData["picture"] as? String
                            isClassifed = batchData["isClassifed"] as? Boolean ?: false
                            aditionalInfo = batchData["aditionalInfo"] as? String ?: ""

                            // Puedes iterar sobre la lista de items dentro del lote
                            val itemsData = batchData["itemsInside"] as? List<Map<String, Any>>
                            itemsInside = itemsData?.map { itemData ->
                                Item().apply {
                                    // Configura los atributos del Item según los datos de Firebase
                                    idItem = itemData["idItem"] as? String ?: ""
                                    attributes = itemData["attributes"] as? MutableList<Attribute> ?: mutableListOf()
                                }
                            }?.toMutableList() ?: mutableListOf()
                        }

                        batchesList.add(batch)
                    }
                }
            }

        } catch (exception: Exception) {
            // Maneja las excepciones aquí
            Log.e("getBatchesForUser", "Error al obtener lotes: $exception")
        }

        return batchesList
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
                        address = user.address?:"",
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

    suspend fun getAllObjetcJewel(): MutableList<Jewel> {
        // Crear una lista mutable para almacenar los objetos Jewel
        val jewelList = mutableListOf<Jewel>()

        try {
            // Obtener una referencia a la colección "jewels"
            val jewelsCollection = Firebase.firestore.collection("jewelsCatalog")

            // Realizar la consulta para obtener todos los documentos de la colección
            val querySnapshot = jewelsCollection.get().await()

            // Iterar sobre los documentos y convertirlos a objetos Jewel
            for (document in querySnapshot.documents) {
                // Convertir el documento a un objeto Jewel y agregarlo a la lista
                val jewel = document.toObject(Jewel::class.java)
                jewel?.let {
                    jewelList.add(it)
                }
            }

        } catch (e: Exception) {
            // Manejar excepciones aquí según tus necesidades
            e.printStackTrace()
        }

        // Devolver la lista de objetos Jewel
        return jewelList
    }


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


    suspend fun getUserEmailByBatchId(batchId: String): String? {
        try {
            // Obtén una referencia a la colección de usuarios
            val usersCollection = db.collection("users")

            // Realiza una consulta para obtener todos los documentos de la colección
            val querySnapshot = usersCollection.get().await()

            // Itera sobre los documentos de la colección de usuarios
            for (userDocument in querySnapshot.documents) {
                // Verifica si el documento del usuario existe y contiene la lista de batches
                if (userDocument.exists()) {
                    val batchesData = userDocument.get("batches") as? List<Map<String, Any>>

                    // Verifica que la lista de batches no sea nula
                    if (batchesData != null) {
                        // Encuentra el lote con el ID deseado
                        val targetBatch = batchesData.find { it["idBatch"] == batchId }

                        // Verifica que el lote con el ID deseado existe
                        if (targetBatch != null) {
                            // Retorna el correo electrónico del usuario
                            return userDocument.id
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            // Maneja las excepciones aquí
            Log.e("getUserEmailByBatchId", "Error al obtener el correo electrónico: $exception")
        }

        // Si no se encuentra el lote, retorna null
        return null
    }


    suspend fun getAllRoles(): ArrayList<String> {
        val rolesCollection = db.collection("roles")
        val rolesList = ArrayList<String>()

        try {
            val querySnapshot = rolesCollection.get().await()

            for (document in querySnapshot.documents) {
                val roleName = document.getString("name")
                roleName?.let {
                    rolesList.add(it)
                }
            }
        } catch (e: Exception) {
            // Manejar la excepción según tus necesidades
            e.printStackTrace()
        }

        return rolesList
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

    suspend fun updateAllDataUser(user: Model.Users.User){
        var us = hashMapOf(
            "name" to user.name,
            "email" to user.email.uppercase(),
            "address" to user.address,
            "phone" to user.phone,
            "picture" to user.picture,
            "role" to user.role,
            "batches" to user.batches
        )
        db.collection("users")
            .document(us.get("email").toString()) //It will be "document key".
            .set(us).addOnSuccessListener {
                Log.d("updateAllDataUser", "User updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("updateAllDataUser", "Error updating user: $e")
            }
    }


    suspend fun updatePhotoByEmail(email: String, newPhotoUrl: String) {
        val userDocument = db.collection("users").document(email)

        try {
            userDocument.update("photo", newPhotoUrl)
                .addOnSuccessListener {
                    Log.d("UpdatePhoto", "Foto actualizada con éxito")
                }
                .addOnFailureListener { e ->
                    Log.e("UpdatePhoto", "Error al actualizar la foto: $e")
                }
        } catch (exception: Exception) {
            Log.e("UpdatePhoto", "Error al obtener datos: $exception")
        }
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


    suspend fun deleteItemFromBatch(batchId: String, itemId: String) {
        try {
            // Obtén una referencia a la colección de usuarios
            val usersCollection = db.collection("users")

            // Realiza una consulta para obtener todos los documentos de la colección
            val querySnapshot = usersCollection.get().await()

            // Itera sobre los documentos de la colección de usuarios
            for (userDocument in querySnapshot.documents) {
                // Verifica si el documento del usuario existe y contiene la lista de batches
                if (userDocument.exists()) {
                    val batchesData = userDocument.get("batches") as? List<MutableMap<String, Any>>

                    // Verifica que la lista de batches no sea nula
                    if (batchesData != null) {
                        // Encuentra el lote con el ID deseado
                        val targetBatch = batchesData.find { it["idBatch"] == batchId }

                        // Verifica que el lote con el ID deseado existe
                        if (targetBatch != null) {
                            // Obtén la lista de itemsInside del batch
                            val itemsInsideData = targetBatch["itemsInside"] as? MutableList<Map<String, Any>>

                            // Verifica que la lista de itemsInside no sea nula
                            if (itemsInsideData != null) {
                                // Filtra los itemsInside para excluir el item con el ID deseado
                                val updatedItemsInside = itemsInsideData.filterNot { it["idItem"] == itemId }.toMutableList()

                                // Actualiza la lista de itemsInside en el lote
                                targetBatch["itemsInside"] = updatedItemsInside

                                // Actualiza el documento del usuario con la nueva información de lotes
                                usersCollection.document(userDocument.id).update("batches", batchesData)
                                    .addOnSuccessListener {
                                        Log.d("deleteItemFromBatch", "Item borrado con éxito")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("deleteItemFromBatch", "Error al borrar item: $e")
                                    }
                            }
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            // Maneja las excepciones aquí
            Log.e("deleteItemFromBatch", "Error al borrar item: $exception")
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


    suspend fun deleteUserByEmailAndRefresh(email: String):ArrayList<Model.Users.User> {
        var updatedUsers = ArrayList<Model.Users.User>()
        try {
            // Referencia al documento del usuario
            val userDocument = db.collection("users").document(email)

            // Borrar el documento del usuario
            userDocument.delete().await()

            // Obtener la lista actualizada de usuarios después de la eliminación
            val updatedUsers = getAllUsers()


            // También puedes agregar aquí cualquier otra lógica que necesites después de borrar el usuario
            Log.d("deleteUserByEmail", "Usuario eliminado con éxito: $email")
        } catch (exception: Exception) {
            Log.e("deleteUserByEmail", "Error al eliminar usuario: $exception")
        }
        return updatedUsers
    }


    suspend fun deleteBatchByIdIfNotReceived(batchId: String) {
        try {
            // Obtén una referencia a la colección de usuarios
            val usersCollection = db.collection("users")

            // Realiza una consulta para obtener todos los documentos de usuarios
            val usersDocuments = usersCollection.get().await()

            // Itera sobre los documentos de usuarios
            for (userDocument in usersDocuments.documents) {
                // Obtiene la lista de lotes del usuario
                val batchesList = userDocument["batches"] as? List<Map<String, Any>>

                // Verifica si la lista de lotes no es nula
                if (batchesList != null) {
                    // Itera sobre los lotes del usuario
                    for (batchMap in batchesList) {
                        // Obtiene el ID del lote actual
                        val currentBatchId = batchMap["idBatch"] as? String

                        // Verifica si el ID del lote actual coincide con el ID que se desea eliminar
                        if (currentBatchId == batchId) {
                            // Obtiene el atributo "received" del lote actual
                            val received = batchMap["received"] as? Boolean

                            // Verifica si el lote no ha sido recibido
                            if (received != true) {
                                // Elimina el lote solo si no ha sido recibido
                                userDocument.reference.update("batches", FieldValue.arrayRemove(batchMap))
                                Log.d("deleteBatchByIdIfNotReceived", "Lote eliminado con éxito: $batchId")
                            } else {
                                Log.d("deleteBatchByIdIfNotReceived", "No se puede eliminar, el lote ha sido recibido: $batchId")
                            }
                            break  // No es necesario seguir buscando en otros lotes
                        }
                    }
                }
            }

        } catch (exception: Exception) {
            // Maneja las excepciones aquí
            Log.e("deleteBatchByIdIfNotReceived", "Error al eliminar lote: $exception")
        }
    }



    suspend fun deleteImageFromStorage(imageId: String, folderPath: String) {
        try {
            val storageRef = Firebase.storage.reference.child("$folderPath/$imageId")

            // Elimina la imagen del almacenamiento
            storageRef.delete().await()

            Log.d("deleteImageFromStorage", "Imagen eliminada con éxito: $imageId")

        } catch (exception: Exception) {
            // Maneja las excepciones aquí
            Log.e("deleteImageFromStorage", "Error al eliminar imagen del almacenamiento: $exception")
        }
    }







}// End of class

