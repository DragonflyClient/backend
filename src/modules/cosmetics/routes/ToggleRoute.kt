package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter

object ToggleRoute : ModuleRoute("toggle", HttpMethod.Post, "jwt", true) {

    override suspend fun Call.handleCall() {
        val account = twoWayAuthentication()
        val body = call.receive<JsonObject>()
        val cosmeticQualifier = body["cosmeticQualifier"].asString
        val enable = body["enable"].asBoolean
        var found = false

        CosmeticsController.updateEach(Filter.new().dragonfly(account.uuid)) {
            if (it.cosmeticQualifier == cosmeticQualifier) {
                it.enabled = enable
                found = true
            }
        }

        if (found) {
            success()
        } else {
            error("Invalid cosmetic qualifier")
        }
    }
}