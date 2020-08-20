package modules.version.routes

import core.*
import io.ktor.http.*
import modules.version.util.update.UpdateHistory

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
object VersionInstallerRoute : ModuleRoute("installer", HttpMethod.Get) {

    override suspend fun Call.handleCall() {
        json {
            "version" * UpdateHistory.installer
        }
    }

    override fun legacyRoute() = "version/installer"
}