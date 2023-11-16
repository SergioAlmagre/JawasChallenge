package Model.Hardware

import Auxiliaries.Attribute

class Item {

    var idItem: Int
    var attributes = mutableListOf<Attribute>()
    init {
        var itemType = Attribute("Type")
        var itemObservations = Attribute("Observations")
        var itemPicture = Attribute("Picture")

        attributes.add(itemType)
        attributes.add(itemObservations)
        attributes.add(itemPicture)
    }

    constructor(
    ) {
        this.idItem = Constants.Ids.idItem
        Constants.Ids.idItem++
    }


    override fun toString(): String {
        return "IdItem=$idItem, attributes=$attributes \n)"
    }


}