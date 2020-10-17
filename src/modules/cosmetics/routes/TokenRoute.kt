package modules.cosmetics.routes

import core.ModuleRoute
import core.json
import io.ktor.application.*
import io.ktor.routing.*
import modules.cosmetics.util.CosmeticsController

object TokenRoute : ModuleRoute("token") {

    override fun Route.setup() {
        get("{payload}") {
            val payload = call.parameters["payload"]!!
            val token = CosmeticsController.getToken(payload)

            json {
                "success" * (token != null)
                "token" * token
            }
        }
    }
}