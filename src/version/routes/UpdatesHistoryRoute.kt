package version.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import version.Version
import version.update.UpdateChannel
import version.update.UpdateHistory

/**
 * Adds a route to view the complete update history since a specific version.
 */
fun Routing.updatesHistory() {
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