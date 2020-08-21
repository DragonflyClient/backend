package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.JwtConfig

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object LoginRoute : ModuleRoute("login", HttpMethod.Post) {

    override suspend fun Call.handleCall() {
        val credentials = call.receive<UserPasswordCredential>()
        val account = AuthenticationManager.verify(credentials.name, credentials.password)
            ?: return call.respond(mapOf(
                "success" to false,
                "error" to "Invalid username or password"
            ))
        val token = JwtConfig.makeToken(account)

        json {
            "success" * true
            "token" * token
            +account
        }
    }

    override fun legacyRoute() = "login"
}