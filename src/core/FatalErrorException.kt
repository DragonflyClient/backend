package core

import io.ktor.http.*

class FatalErrorException(
    message: String,
    val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) : Exception(message)