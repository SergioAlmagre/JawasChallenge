package Model.Users

import Model.Hardware.Batch
import java.io.Serializable

open class User: Serializable {

    var batches: MutableList<Batch> = mutableListOf()
    var name: String
    var email: String
    var address: String?
    var phone: String?
    var picture: String?
    var role: String?

    constructor() {
        // Constructor sin argumentos necesario para la deserialización de Firebase
        this.name = ""
        this.email = ""
        this.address = null
        this.phone = null
        this.picture = null
        this.role = ""
    }

    constructor(
        name: String,
        email: String,
        address: String? = null,
        phone: String? = null,
        picture: String? = null,
        role: String
    ) {
        this.name = name
        this.email = email
        this.address = address
        this.phone = phone
        this.picture = picture
        this.role = role
    }

    fun addBatch(batch: Batch) {
        batches.add(batch)
    }

}