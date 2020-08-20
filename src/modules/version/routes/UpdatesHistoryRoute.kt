package modules.version.routes

import core.ModuleRoute
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.version.util.Version
import modules.version.util.update.UpdateChannel
import modules.version.util.update.UpdateHistory

/**
 * Adds a route to view the complete update history since a specific version.
 */
object UpdatesHistoryRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        get("updates/history/") {
            if (call.parameters.contains("channel")) {
                val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!) ?: error("Invalid channel identifier")
                val since = Version.of(call.parameters["since"] ?: "0.0.0.0") ?: error("Invalid 'since' parameter")
                val history = UpdateHistory.getUpdateHistorySince(channel, since)

                call.respond(history)
            } else error("Missing 'channel' parameter")
        }
    }
}