package modules.auth.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.auth.util.Authentication
import modules.auth.util.JwtConfig

/**
 * Creates a /auth root to validate JWTs.
 */
fun Routing.routeAuthCookie() {
    post("/cookie/auth") {
        val cookie = call.request.cookies["dragonfly-token"] ?: error("No token cookie found")
        val token = JwtConfig.verifier.verify(cookie)
        val account = token.getClaim("uuid").asString()
            ?.let { uuid -> Authentication.getByUUID(uuid) }

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
