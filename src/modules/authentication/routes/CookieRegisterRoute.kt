package modules.authentication.routes

import core.ModuleRoute
import core.respondToken
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import modules.authentication.util.Authentication

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object CookieRegisterRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        post("/cookie/register") {
            val credentials = call.receive<UserPasswordCredential>()
            val account = Authentication.register(credentials.name, credentials.password)

            respondToken(account)
        }
    }
}
