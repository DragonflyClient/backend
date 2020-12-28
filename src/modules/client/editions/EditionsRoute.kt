package modules.client.editions

import core.CallContext
import core.ModuleRoute
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import modules.client.ClientModule

class EditionsRoute : ModuleRoute("editions", HttpMethod.Get) {
    override suspend fun CallContext.handleCall() {
        call.respond(ClientModule.editionsService.getEditions())
    }
}