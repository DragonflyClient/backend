package modules.authentication.routes

import core.ModuleRoute
import core.respondAccount
import io.ktor.application.*
import io.ktor.routing.*
import modules.authentication.util.Authentication
import modules.authentication.util.JwtConfig

/**
 * Creates a /auth root to validate JWTs.
 */
object CookieAuthRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        post("/cookie/auth") {
            val cookie = call.request.cookies["dragonfly-token"] ?: error("No token cookie found")
            val token = JwtConfig.verifier.verify(cookie)
            val account = token.getClaim("uuid").asString()?.let { uuid -> Authentication.getByUUID(uuid) }

            respondAccount(account)
        }
    }
}