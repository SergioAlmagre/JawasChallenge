package Model.Hardware

import Auxiliaries.Attribute
import java.util.UUID

class Item {

    var idItem: String
    var attributes = mutableListOf<Attribute>()
    init {
        var itemType = Attribute("Type")
        var itemDescription = Attribute("Description")
        var itemPicture = Attribute("Picture")

        attributes.add(itemType)
        attributes.add(itemDescription)
        attributes.add(itemPicture)
    }

    constructor(
    ) {
        this.idItem = generateUniqueId()
    }


    override fun toString(): String {
        return "IdItem=$idItem, attributes=$attributes \n)"
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }


}