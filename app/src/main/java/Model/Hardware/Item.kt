package Model.Hardware

class Item {



    var idItem: Int
    var itemType: String
    var itemBrand: String?
    var itemModel: String?
    var itemObservations: String?
    var componentsFrom = mutableListOf<String>()
    var itemPicture: String?

    constructor(itemType: String, itemBrand: String?, itemModel: String?, itemObservations: String?, itemPicture: String?) {
        this.idItem = Constants.Ids.idItem
        this.itemType = itemType
        this.itemBrand = itemBrand
        this.itemModel = itemModel
        this.itemObservations = itemObservations
        this.itemPicture = itemPicture
        Constants.Ids.idItem++
    }

    fun addComponent(component: String) {
        componentsFrom.add(component)
    }

    fun removeComponent(component: String) {
        componentsFrom.remove(component)
    }


    override fun toString(): String {
        return "Item: ${itemType}, ${itemBrand}, ${itemModel}, ${itemObservations}, ${itemPicture}, ${componentsFrom}"
    }


}
