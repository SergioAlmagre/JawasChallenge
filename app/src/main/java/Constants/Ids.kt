package Constants

import java.util.UUID

object Ids {

    var idBatch: String = generateUniqueId()
    var idItem: Int = 0


    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}