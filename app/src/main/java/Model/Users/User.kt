package Model.Users

open class User {

    var name: String
    var email: String
    var address: String?
    var phone: String?
    var picture: String?
    var role: String?

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








}