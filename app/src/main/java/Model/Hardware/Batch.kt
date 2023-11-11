package Model.Hardware

import java.util.Date

class Batch {
    val idBatch: Int
    val userName:String
    var latitude:Double
    var longitude:Double
    val creationDate: String
    var delivered:Boolean
    var pickUpDate:String
    var picture:String?
    var isClassifed:Boolean = false
    var itemsInside: MutableList<Item> = mutableListOf()

    constructor(
        userName: String,
        latitude: Double,
        longitude: Double,
        creationDate: String,
        delivered: Boolean,
        pickUpDate: String,
        picture: String?,
        isClassifier:Boolean
    ) {
        this.idBatch = Constants.Ids.idBatch
        this.userName = userName
        this.latitude = latitude
        this.longitude = longitude
        this.creationDate = creationDate
        this.delivered = delivered
        this.pickUpDate = pickUpDate
        this.picture = picture
        this.isClassifed = isClassifier // this option only must be change to true if the batch is classified
        Constants.Ids.idBatch++
    }

    fun addItem(item: Item) {
        itemsInside.add(item)
    }

    // this option only must be push if the batch is classified
    fun addAllItemsBatchToStore() {
        for (item in itemsInside) {
            Store.ItemsStore.itemsList.add(item)
        }
    }


}

