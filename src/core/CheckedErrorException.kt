package core

import io.ktor.http.*

class CheckedErrorException(
    message: String,
    val statusCode: HttpStatusCode,
    val errorCode: String?
) : Exception(message)