package Auxiliaries

data class ObjectQuantity(var name: String, var quantity: Int) {

    constructor() : this("", 0)//Needed for Firebase

    override fun toString(): String {
        return "ObjectQuantity(name='$name', quantity=$quantity)\n"
    }
}