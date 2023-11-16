package Auxiliaries

data class ObjectQuantity(var name: String, var quantity: Long) {

    override fun toString(): String {
        return "ObjectQuantity(name='$name', quantity=$quantity)\n"
    }
}