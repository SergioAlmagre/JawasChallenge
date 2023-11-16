package Model.Users

import Model.Hardware.Batch

class Donor: User {

    var batches: MutableList<Batch> = mutableListOf()

    constructor() : super("", "", null, null, null, "") {
        // Constructor sin argumentos necesario para la deserialización de Firebase
    }
    constructor(
        name: String,
        email: String,
        address: String?,
        phone: String?,
        picture: String?,
        role: String
    ) : super(name, email, address, phone, picture, role)


    fun addBatch(batch: Batch) {
        batches.add(batch)
    }

    fun removeBatch(batch: Batch) {
        batches.remove(batch)
    }

}