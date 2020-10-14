package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter

object ToggleRoute : ModuleRoute("toggle", HttpMethod.Post, "jwt", true) {

    override suspend fun CallContext.handleCall() {
        val account = getAccount()
        val body = call.receive<JsonObject>()
        val cosmeticQualifier = body["cosmeticQualifier"].asString
        val enable = body["enable"].asBoolean

        val updated = CosmeticsController.updateEach(
            Filter.new().dragonfly(account.uuid), cosmeticQualifier
        ) { it.enabled = enable }

        if (updated) success() else checkedError("Invalid cosmetic qualifier", HttpStatusCode.BadRequest)
    }
}