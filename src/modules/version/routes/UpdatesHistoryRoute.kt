package modules.version.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import modules.version.util.Version
import modules.version.util.update.UpdateChannel
import modules.version.util.update.UpdateHistory

/**
 * Adds a route to view the complete update history since a specific version.
 */
object UpdatesHistoryRoute : ModuleRoute("updates/history", HttpMethod.Get) {

    override suspend fun Call.handleCall() {
        if (call.parameters.contains("channel")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!) ?: fatal("Invalid channel identifier")
            val since = Version.of(call.parameters["since"] ?: "0.0.0.0") ?: fatal("Invalid 'since' parameter")
            val history = UpdateHistory.getUpdateHistorySince(channel, since)

            call.respond(history)
        } else fatal("Missing 'channel' parameter")
    }

    override fun legacyRoute() = "updates/history"
}