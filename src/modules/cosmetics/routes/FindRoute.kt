package modules.cosmetics.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter
import modules.cosmetics.util.models.CosmeticItem

object FindRoute : ModuleRoute("find", HttpMethod.Get) {

    override suspend fun CallContext.handleCall() {
        val dragonflyUUID = call.parameters["dragonfly"]
        val cosmetics: List<CosmeticItem> = if (dragonflyUUID != null) {
            CosmeticsController.find(Filter.new().dragonfly(dragonflyUUID))
        } else {
            val minecraftUUID = (call.parameters["uuid"] ?: call.parameters["minecraft"])
                ?: checkedError("No UUID specified", HttpStatusCode.BadRequest)
            CosmeticsController.find(Filter.new().minecraft(minecraftUUID))
        }

        json {
            "success" * true
            "cosmetics" * cosmetics
        }
    }
}