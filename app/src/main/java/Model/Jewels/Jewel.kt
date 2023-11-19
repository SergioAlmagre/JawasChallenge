package Model.Jewels

import Auxiliaries.ObjectQuantity

class Jewel {

    var name:String
    var instructions: String?
    var price: Double?
    var picture: String? = null

    var components: MutableList<ObjectQuantity> = mutableListOf()


    constructor(name: String,
                instruction: String? = null,
                price: Double?  = null,
                picture: String?  = null
    ) {
        this.name = name.uppercase()
        this.instructions = instruction
        this.price = price
        this.picture = picture
    }

    constructor() { //Needed for Firebase
        this.name = ""
        this.instructions = ""
        this.price = 0.0
        this.picture = ""
    }

    fun addInstructions(instruction: String) {
        instructions = instruction
    }

    fun addObjetcQuantity(objectQuantity: ObjectQuantity) {
        components.add(objectQuantity)
    }

    fun removeObjetcQuantity(objectQuantity: ObjectQuantity) {
        components.remove(objectQuantity)
    }

    override fun toString(): String {
        var cad = ""
        cad += "Jewel name: ${name} "

        cad += "\n" + "Components: "
        for (component in components) {
            cad += "\n" + component + ", "
        }

        cad += "\n" + "Instructions: ${instructions} \n"
        return cad

        cad += "\n" + "Price: ${price} \n"
    }




}