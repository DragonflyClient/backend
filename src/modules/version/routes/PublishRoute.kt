package modules.version.routes

import core.ModuleRoute
import core.success
import dragonfly.updates.rmi.RMIClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import modules.version.util.update.*

/**
 * Adds a route allowing the user to publish updates.
 */
object PublishRoute : ModuleRoute("publish", HttpMethod.Get, "master") {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
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
}