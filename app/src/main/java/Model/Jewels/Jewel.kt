package Model.Jewels

class Jewel {

    var name:String
    var instructions: String?
    var price: Double?
    var picture: String? = null

    var components: MutableList<String> = mutableListOf()


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


    fun addInstructions(instruction: String) {
        instructions = instruction
    }

    fun addComponent(component: String) {
        components.add(component)
    }

    fun removeComponent(component: String) {
        components.remove(component)
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