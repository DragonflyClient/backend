package modules.authentication.routes

import core.ModuleRoute
import core.respondAccount
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import modules.authentication.util.Authentication
import modules.authentication.util.JwtConfig

/**
 * Creates a /auth root to validate JWTs.
 */
object CookieTokenRoute : ModuleRoute("cookie/token", HttpMethod.Post) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        val cookie = call.request.cookies["dragonfly-token"] ?: error("No token cookie found")
        val token = JwtConfig.verifier.verify(cookie)
        val account = token.getClaim("uuid").asString()?.let { uuid -> Authentication.getByUUID(uuid) }

        respondAccount(account)
    }

    override fun legacyRoute() = "cookie/auth"
}