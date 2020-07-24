package modules.auth.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.auth.JwtConfig

/**
 * Creates a /auth root to validate JWTs.
 */
fun Routing.routeAuthCookie() {
    post("/cookie/auth") {
        val cookie = call.request.cookies["dragonfly-token"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "No token cookie found")
        val token = JwtConfig.verifier.verify(cookie)
        val account = token.getClaim("identifier").asString()
            ?.let { identifier -> modules.auth.Authentication.getByUsername(identifier) }

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