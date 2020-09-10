package modules.cosmetics.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import modules.cosmetics.util.*

object FindRoute : ModuleRoute("find", HttpMethod.Get) {

    override suspend fun Call.handleCall() {
        val dragonflyUUID = call.parameters["dragonfly"]
        val cosmetics: List<CosmeticItem> = if (dragonflyUUID != null) {
            CosmeticsController.find(Filter.new().dragonfly(dragonflyUUID))
        } else {
            val minecraftUUID = (call.parameters["uuid"] ?: call.parameters["minecraft"]) ?: error("No UUID specified")
            CosmeticsController.find(Filter.new().minecraft(minecraftUUID))
        }

        json {
            "success" * true
            "cosmetics" * cosmetics
        }
    }
}