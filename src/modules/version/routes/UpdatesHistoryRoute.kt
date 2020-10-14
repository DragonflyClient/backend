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

    override suspend fun CallContext.handleCall() {
        if (call.parameters.contains("channel")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!) ?: checkedError("Invalid channel identifier")
            val since = Version.of(call.parameters["since"] ?: "0.0.0.0") ?: checkedError("Invalid 'since' parameter")
            val history = UpdateHistory.getUpdateHistorySince(channel, since)

            call.respond(history)
        } else checkedError("Missing 'channel' parameter")
    }

    override fun legacyRoute() = "updates/history"
}