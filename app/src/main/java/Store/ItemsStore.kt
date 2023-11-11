package Store

import Model.Hardware.Item

object ItemsStore {

    var itemsList: MutableList<Item> = mutableListOf()

    override fun toString(): String {
        return "ItemsStore: ${itemsList} \n"



    }
}