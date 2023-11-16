package Auxiliaries

data class Attribute(var name: String, var content: String? = null) {
    constructor() : this("")

    override fun toString(): String {
        return "Attribute(name='$name', content=$content)\n"
    }
}