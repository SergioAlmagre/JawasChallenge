package Model.Hardware

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class Batch {
    val idBatch: String
    val userName: String
    var latitude: Double?
    var longitude: Double?
    var address: String?

    private val date = LocalDateTime.now()
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    val creationDate: String

    var received: Boolean
    var picture: String?
    var isClassifed: Boolean = false
    var itemsInside: MutableList<Item> = mutableListOf()


    constructor(
        userName: String,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
        picture: String? = null,
    ) {
        this.idBatch = generateUniqueId()
        this.userName = userName
        this.latitude = latitude
        this.longitude = longitude
        this.address = address
        this.creationDate = date.format(formatter)
        this.received = false
        this.picture = picture
        this.isClassifed = false // this option only must be change to true if the batch is classified
    }

    constructor() : this(
        "",
        null,
        null,
        null,
        null)


    fun addItem(item: Item) {
        itemsInside.add(item)
    }

    // this option only must be push if the batch is classified
//    fun classify() {
//        for (item in itemsInside) {
//            Store.ItemsStore.itemsList.add(item)
//        }
//        Store.PendingBatches.batchList.remove(this)
//    }

    override fun toString(): String {
        return "Batch(idBatch=$idBatch, userName='$userName', latitude=$latitude, longitude=$longitude, creationDate='$date', delivered=$received,  picture=$picture, isClassifed=$isClassifed, itemsInside=$itemsInside\n\n)"
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }


}

