package modules.keys

import org.bson.types.ObjectId

data class KeyDocument(
    var key: String,
    var attached: Boolean,
    var createdOn: Long,
    var machineIdentifier: String?,
    val _id: ObjectId? = null
)
