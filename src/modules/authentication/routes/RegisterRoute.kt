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
 * Creates a /register route to create new accounts.
 */
object RegisterRoute : ModuleRoute("register", HttpMethod.Post) {

    override suspend fun Call.handleCall() {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            val account = AuthenticationManager.register(credentials.name, credentials.password)
            val token = JwtConfig.makeToken(account)

            json {
                "success" * true
                "token" * token
                +account
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    override fun legacyRoute() = "register"
}