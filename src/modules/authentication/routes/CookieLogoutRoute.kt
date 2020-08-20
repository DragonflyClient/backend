package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.date.*

/**
 * Creates a /logout route that deletes the authentication cookie.
 */
object CookieLogoutRoute : ModuleRoute("cookie/logout", HttpMethod.Post) {

    override suspend fun Call.handleCall() {
        call.response.cookies.append(Cookie(
            name = "dragonfly-token",
            value = "",
            httpOnly = true,
            secure = true,
            expires = GMTDate(0), // in the past
            domain = "playdragonfly.net",
            path = "/",
            extensions = mapOf(
                "SameSite" to "Strict"
            )
        ))

        success()
    }

    override fun legacyRoute() = "cookie/logout"
}