package modules.cosmetics.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter

object FindRoute : ModuleRoute("find", HttpMethod.Get) {

    override suspend fun Call.handleCall() {
        val minecraftUUID = call.parameters["uuid"] ?: error("No UUID specified")
        val cosmetics = CosmeticsController.find(Filter.new().minecraft(minecraftUUID))

        json {
            "success" * true
            "cosmetics" * cosmetics
        }
    }
}