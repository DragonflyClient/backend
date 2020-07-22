package modules.version.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.version.update.*

/**
 * Adds a route allowing the user to publish updates.
 */
fun Routing.routeVersionPublish() {
    authenticate("master") {
        post("/publish/eap") {
            val update = call.receive<Update>()
            UpdateHistory.publishUpdate(UpdateChannel.EARLY_ACCESS_PROGRAM, update)
            call.respond(mapOf(
                "success" to true
            ))
        }
        post("/publish/stable") {
            val update = call.receive<Update>()
            UpdateHistory.publishUpdate(UpdateChannel.STABLE, update)
            call.respond(mapOf(
                "success" to true
            ))
        }
    }
}