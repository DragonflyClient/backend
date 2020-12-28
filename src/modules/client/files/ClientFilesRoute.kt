package modules.client.files

import core.CallContext
import core.ModuleRoute
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import modules.client.ClientModule

class ClientFilesRoute : ModuleRoute("files", HttpMethod.Get) {

    override suspend fun CallContext.handleCall() {
        call.respond(ClientModule.filesService.fileIndex)
    }
}