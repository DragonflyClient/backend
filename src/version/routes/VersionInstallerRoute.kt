package version.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import version.update.UpdateHistory

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
fun Routing.routeVersionInstaller() {
    get("/version/installer") {
        call.respond(mapOf(
            "version" to UpdateHistory.installer
        ))
    }
}