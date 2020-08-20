package modules.auth.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.auth.util.Account

/**
 * Creates a /auth root to validate JWTs.
 */
fun Routing.routeAuth() {
    authenticate("jwt", optional = true) {
        post("/auth") {
            val account = call.authentication.principal<Account>()

            if (account == null) {
                call.respond(mapOf(
                    "success" to false,
                    "error" to "Unauthenticated"
                ))
            } else {
                call.respond(mapOf(
                    "success" to true,
                    "identifier" to account.identifier,
                    "username" to account.username,
                    "creationDate" to account.creationDate,
                    "permissionLevel" to account.permissionLevel
                ))
            }
        }
    }
}