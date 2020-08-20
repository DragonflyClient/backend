package modules.version.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.version.util.Version
import modules.version.util.update.UpdateChannel
import modules.version.util.update.UpdateHistory

/**
 * Adds a route to view the complete update history since a specific version.
 */
fun Routing.routeVersionUpdatesHistory() {
    get("updates/history/") {
        if (call.parameters.contains("channel")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!) ?: return@get
            val since = Version.of(call.parameters["since"] ?: "0.0.0.0") ?: return@get
            val history = UpdateHistory.getUpdateHistorySince(channel, since)

            call.respond(history)
        } else call.respond(mapOf(
            "error" to "Missing parameters"
        ))
    }
}