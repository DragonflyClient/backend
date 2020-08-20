package modules.authentication.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.util.date.*
import success

/**
 * Creates a /logout route that deletes the authentication cookie.
 */
fun Routing.routeAuthCookieLogout() {
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
