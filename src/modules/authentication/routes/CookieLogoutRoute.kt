package modules.authentication.routes

import core.ModuleRoute
import core.success
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.util.date.*

/**
 * Creates a /logout route that deletes the authentication cookie.
 */
object CookieLogoutRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        post("/cookie/logout") {
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
    }
}