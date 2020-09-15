package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.*

/**
 * Creates a /register route to create new accounts.
 */
object RegisterRoute : ModuleRoute("register", HttpMethod.Post) {

    override suspend fun Call.handleCall() {
        val data = call.receive<RegistrationData>()
        val isVerified = AuthenticationManager.verifyEmail(data.email, data.code)

        if (!isVerified) fatal("Email address isn't verified!", HttpStatusCode.BadRequest)

        val account = AuthenticationManager.register(data.email, data.username, data.password)
        val token = JwtConfig.makeToken(account)

        json {
            "success" * true
            "token" * token
            +account
        }
    }

    override fun legacyRoute() = "register"
}