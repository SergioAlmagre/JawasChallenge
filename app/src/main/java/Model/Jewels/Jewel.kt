package Model.Jewels

class Jewel {

    var name:String
    var components: MutableList<String> = mutableListOf()
    var instructions: String?


    constructor(name: String, instruction: String?) {
        this.name = name
        this.instructions = instruction
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
    }




}