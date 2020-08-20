package modules.authentication.routes

import core.ModuleRoute
import core.respondToken
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import modules.authentication.util.Authentication

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object CookieLoginRoute : ModuleRoute("cookie/login", HttpMethod.Post) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        val credentials = call.receive<UserPasswordCredential>()
        val account = Authentication.verify(credentials.name, credentials.password)
            ?: error("Invalid username or password")

        respondToken(account)
    }

    override fun legacyRoute() = "cookie/login"
}
