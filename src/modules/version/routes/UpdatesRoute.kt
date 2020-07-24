package modules.version.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.version.Version
import modules.version.update.UpdateChannel
import modules.version.update.UpdateHistory

/**
 * Adds a more advanced route of checking for updates by saving the complete update history of
 * Dragonfly. This also fixes an issue with the `requireInstaller` property.
 */
fun Routing.routeVersionUpdates() {
    get("/updates") {
        if (call.parameters.contains("channel") && call.parameters.contains("since")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!) ?: return@get
            val since = Version.of(call.parameters["since"]!!) ?: return@get

            val history = UpdateHistory.getUpdateHistorySince(channel, since)
            val latest = history.firstOrNull() ?: UpdateHistory.getUpdateHistory(channel).first()

            call.respond(
                mapOf(
                    "version" to latest.version,
                    "patchNotes" to latest.patchNotes,
                    "requiresInstaller" to history.any { it.requiresInstaller == true },
                    "releaseDate" to latest.releaseDate
                )
            )
        } else call.respond(mapOf(
            "error" to "Missing parameters"
        ))
    }
}