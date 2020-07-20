package auth.routes

import auth.Authentication
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Creates a /register route to create new accounts.
 */
fun Routing.routeAuthRegister() {
    post("/register") {
        try {
            val credentials = call.receive<Parameters>()
            Authentication.register(credentials["name"]!!, credentials["password"]!!)
            call.respond(mapOf(
                "success" to true
            ))
        } catch (e: Exception) {
            call.respond(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}