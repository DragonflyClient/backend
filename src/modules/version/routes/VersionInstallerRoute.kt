package modules.version.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.version.util.update.UpdateHistory

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