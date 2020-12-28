package modules.launcher.editions

import core.CallContext
import core.ModuleRoute
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import modules.launcher.LauncherModule

class DragonflyEditionsRoute : ModuleRoute("editions", HttpMethod.Get) {
    override suspend fun CallContext.handleCall() {
        call.respond(LauncherModule.editionsService.getEditions())
    }
}