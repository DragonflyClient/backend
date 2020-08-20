package modules.version.routes

import core.ModuleRoute
import core.json
import io.ktor.routing.*
import modules.version.util.update.UpdateHistory

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
object VersionInstallerRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        get("/version/installer") {
            json {
                "version" * UpdateHistory.installer
            }
        }
    }
}