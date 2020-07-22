package modules.auth.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.auth.Authentication

/**
 * Creates a /register route to create new accounts.
 */
fun Routing.routeAuthRegister() {
    post("/register") {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            Authentication.register(credentials.name, credentials.password)
            call.respond(mapOf(
                "success" to true
            ))
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}