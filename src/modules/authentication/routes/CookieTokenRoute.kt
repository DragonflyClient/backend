package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.JwtConfig

/**
 * Creates a /auth root to validate JWTs.
 */
object CookieTokenRoute : ModuleRoute("cookie/token", HttpMethod.Post) {

    override suspend fun CallContext.handleCall() {
        val cookie = call.request.cookies["dragonfly-token"] ?: checkedError("No token cookie found")
        val token = JwtConfig.verifier.verify(cookie)
        val account = token.getClaim("uuid").asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }

        respondAccount(account)
    }
}