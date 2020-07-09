package version.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import version.UpdateChannel
import version.VersionManager

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
fun Routing.version() {
    get("/version") {
        if (call.parameters.contains("channel")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!)

            if (channel == null) {
                call.respond(mapOf("error" to "Invalid channel type '${call.parameters["channel"]}"))
                return@get
            } else if (channel == UpdateChannel.EARLY_ACCESS_PROGRAM) {
                call.respond(VersionManager.earlyAccess)
                return@get
            }
        }

        call.respond(VersionManager.stable)
    }
}