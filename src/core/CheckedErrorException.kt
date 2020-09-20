package core

import io.ktor.http.*

class CheckedErrorException(
    message: String,
    val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) : Exception(message)