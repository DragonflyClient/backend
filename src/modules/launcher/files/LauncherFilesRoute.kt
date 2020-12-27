package modules.launcher.files

import core.CallContext
import core.ModuleRoute
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import modules.launcher.LauncherModule

class LauncherFilesRoute : ModuleRoute("files", HttpMethod.Get) {

    override suspend fun CallContext.handleCall() {
        call.respond(LauncherModule.filesService.fileIndex)
    }
}