package modules.cosmetics.routes

import core.*
import io.ktor.http.*
import modules.cosmetics.util.CosmeticsController

object AvailableRoute : ModuleRoute("available", HttpMethod.Get) {

    override suspend fun Call.handleCall() {
        val availableCosmetics = CosmeticsController.getAvailable()

        json {
            "success" * true
            "availableCosmetics" * availableCosmetics
        }
    }
}