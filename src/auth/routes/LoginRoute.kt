package auth.routes

import auth.Authentication
import auth.JwtConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
fun Routing.routeAuthLogin() {
    post("/login") {
        val credentials = call.receive<UserPasswordCredential>()
        val account = Authentication.verify(credentials.name, credentials.password)
            ?: return@post call.respond(mapOf(
                "success" to false,
                "error" to "Invalid username or password"
            ))
        val token = JwtConfig.makeToken(account)
        call.respond(mapOf(
            "identifier" to account.identifier,
            "username" to account.username,
            "creationDate" to account.creationDate,
            "permissionLevel" to account.permissionLevel,
            "token" to token
        ))
    }
}