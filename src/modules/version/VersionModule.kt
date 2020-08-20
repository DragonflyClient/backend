package modules.version

import core.Module
import io.ktor.routing.*
import modules.version.routes.*

object VersionModule : Module() {

    override fun Routing.provideRouting() {
        routeVersion()
        routeVersionInstaller()
        routeVersionUpdates()
        routeVersionUpdatesHistory()
        routeVersionPublish()
    }
}