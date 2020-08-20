package modules.authentication.routes

import core.ModuleRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.authentication.util.Authentication
import modules.authentication.util.JwtConfig

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object LoginRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        post("/login") {
            val credentials = call.receive<UserPasswordCredential>()
            val account = Authentication.verify(credentials.name, credentials.password)
                ?: return@post call.respond(mapOf(
                    "success" to false,
                    "error" to "Invalid username or password"
                ))
            val token = JwtConfig.makeToken(account)
            call.respond(mapOf(
                "success" to true,
                "identifier" to account.identifier,
                "username" to account.username,
                "creationDate" to account.creationDate,
                "permissionLevel" to account.permissionLevel,
                "token" to token
            ))
        }
    }
}