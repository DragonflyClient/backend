package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter

object BindRoute : ModuleRoute("bind", HttpMethod.Post, "jwt", true) {

    override suspend fun Call.handleCall() {
        val account = twoWayAuthentication()
        val body = call.receive<JsonObject>()
        val cosmeticQualifier = body["cosmeticQualifier"].asString
        val minecraftUUID = if (body.has("unbind")) null else body["minecraftUUID"].asString

        if (minecraftUUID != null && account.linkedMinecraftAccounts?.contains(minecraftUUID) != true) {
            error("This Minecraft account is not linked to the Dragonfly account.")
        }

        val updated = CosmeticsController.updateEach(
            Filter.new().dragonfly(account.uuid),
            cosmeticQualifier
        ) { it.minecraft = minecraftUUID }

        if (updated) success() else fatal("Invalid cosmetic qualifier", HttpStatusCode.BadRequest)
    }
}