package modules.version.routes

import core.*
import dragonfly.updates.rmi.RMIClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.version.util.update.*

/**
 * Adds a route allowing the user to publish updates.
 */
object PublishRoute : ModuleRoute("publish", HttpMethod.Get, "master") {

    override suspend fun Call.handleCall() {
        val update = call.receive<Update>()
        val earlyAccess = call.parameters["eap"]?.toBoolean() ?: checkedError("Missing parameter 'eap' of type boolean")
        val stable = call.parameters["stable"]?.toBoolean() ?: checkedError("Missing parameter 'stable' of type boolean")

        if (earlyAccess) {
            UpdateHistory.publishUpdate(UpdateChannel.EARLY_ACCESS_PROGRAM, update)
        }

        if (stable) {
            UpdateHistory.publishUpdate(UpdateChannel.STABLE, update)
        }

        RMIClient.invokePublishUpdate(update, earlyAccess, stable)
        success()
    }

    override fun legacyRoute() = "publish"
}