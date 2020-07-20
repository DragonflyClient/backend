package auth.routes

import auth.Authentication
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
fun Routing.routeAuthRegister() {
    post("register") {
        val credentials = call.receive<UserPasswordCredential>()
        try {
            Authentication.register(credentials.name, credentials.password)
            call.respond(mapOf(
                "success" to true
            ))
        } catch (e: IllegalArgumentException) {
            call.respond(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}