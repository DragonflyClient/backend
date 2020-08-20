package modules.version.routes

import dragonfly.updates.rmi.RMIClient
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import modules.version.util.update.*
import success

/**
 * Adds a route allowing the user to publish updates.
 */
fun Routing.routeVersionPublish() {
    authenticate("master") {
        post("/publish") {
            val update = call.receive<Update>()
            val earlyAccess = call.parameters["eap"]?.toBoolean() ?: error("Missing parameter 'eap' of type boolean")
            val stable = call.parameters["stable"]?.toBoolean() ?: error("Missing parameter 'stable' of type boolean")

            if (earlyAccess) {
                UpdateHistory.publishUpdate(UpdateChannel.EARLY_ACCESS_PROGRAM, update)
            }

            if (stable) {
                UpdateHistory.publishUpdate(UpdateChannel.STABLE, update)
            }

            RMIClient.invokePublishUpdate(update, earlyAccess, stable)
            success()
        }

        post("/publish/eap") {
            error("Please use the new global /publish route!")
        }

        post("/publish/stable") {
            error("Please use the new global /publish route!")
        }
    }
}
