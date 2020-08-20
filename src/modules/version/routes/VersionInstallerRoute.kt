package modules.version.routes

import core.ModuleRoute
import core.json
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import modules.version.util.update.UpdateHistory

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
object VersionInstallerRoute : ModuleRoute("installer", HttpMethod.Get) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        json {
            "version" * UpdateHistory.installer
        }
    }

    override fun legacyRoute() = "version/installer"
}