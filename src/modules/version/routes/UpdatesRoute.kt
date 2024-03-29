package modules.version.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import modules.version.util.Version
import modules.version.util.update.UpdateChannel
import modules.version.util.update.UpdateHistory

/**
 * Adds a more advanced route of checking for updates by saving the complete update history of
 * Dragonfly. This also fixes an issue with the `requireInstaller` property.
 */
object UpdatesRoute : ModuleRoute("updates", HttpMethod.Get) {

    override suspend fun CallContext.handleCall() {
        if (call.parameters.contains("channel") && call.parameters.contains("since")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!) ?: checkedError("Invalid channel identifier")
            val since = Version.of(call.parameters["since"]!!) ?: checkedError("Invalid 'since' parameter")

            val history = UpdateHistory.getUpdateHistorySince(channel, since)
            val latest = history.firstOrNull() ?: UpdateHistory.getUpdateHistory(channel).first()

            call.respond(
                mapOf(
                    "version" to latest.version,
                    "patchNotes" to latest.patchNotes,
                    "requiresInstaller" to history.any { it.requiresInstaller == true },
                    "releaseDate" to latest.releaseDate,
                    "title" to latest.title
                )
            )
        } else checkedError("Missing 'channel' or 'since' parameter")
    }

    override fun getLegacyRoute(): String? = "updates"
}