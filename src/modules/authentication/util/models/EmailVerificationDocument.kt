package modules.authentication.util.models

import org.bson.types.ObjectId

data class EmailVerificationDocument(
    val _id: ObjectId,
    val email: String,
    val code: String,
    var status: String,
    val partner: String? = null
)