package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object CookieLoginRoute : ModuleRoute("cookie/login", HttpMethod.Post) {

    override suspend fun Call.handleCall() {
        val credentials = call.receive<UserPasswordCredential>()
        val account = AuthenticationManager.verify(credentials.name, credentials.password)
            ?: checkedError("Invalid username or password")

        respondToken(account)
    }

    override fun legacyRoute() = "cookie/login"
}
