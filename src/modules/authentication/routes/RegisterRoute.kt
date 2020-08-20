package modules.authentication.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.authentication.util.Authentication
import modules.authentication.util.JwtConfig

/**
 * Creates a /register route to create new accounts.
 */
fun Routing.routeAuthRegister() {
    post("/register") {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            val account = Authentication.register(credentials.name, credentials.password)
            val token = JwtConfig.makeToken(account)

            call.respond(mapOf(
                "success" to true,
                "token" to token,
                "identifier" to account.identifier,
                "username" to account.username,
                "creationDate" to account.creationDate,
                "permissionLevel" to account.permissionLevel
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