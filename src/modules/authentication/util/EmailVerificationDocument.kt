package modules.authentication.util

import org.bson.types.ObjectId

data class EmailVerificationDocument(
    val _id: ObjectId,
    val email: String,
    val code: String,
    var status: String
)