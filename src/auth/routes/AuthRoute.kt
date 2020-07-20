package auth.routes

import auth.Account
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Creates a /auth root to validate JWTs.
 */
fun Routing.routeAuth() {
    authenticate("jwt") {
        post("/auth") {
            val account = call.authentication.principal<Account>()!!
            call.respond(mapOf(
                "identifier" to account.identifier,
                "username" to account.username,
                "creationDate" to account.creationDate,
                "permissionLevel" to account.permissionLevel
            ))
        }
    }
}