package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter
import org.bson.Document

object TransferRoute : ModuleRoute("transfer", HttpMethod.Post, "jwt", true) {

    override suspend fun CallContext.handleCall() {
        val account = requireAccount()
        val body = call.receive<JsonObject>()
        val cosmeticQualifier = body["cosmeticQualifier"]?.asString ?: checkedError("Please submit a cosmetic qualifier")
        val dragonflyUsername = body["dragonflyUsername"]?.asString ?: checkedError("Please submit the target Dragonfly username")
        val resetConfig = body.has("resetConfig") && body["resetConfig"].asBoolean

        val receiverAccount = AuthenticationManager.getByUsername(dragonflyUsername, true)
            ?: checkedError("Could not find a Dragonfly account with the name '$dragonflyUsername'")

        val cosmeticItem = CosmeticsController.find(Filter.new().dragonfly(account.uuid))
            .singleOrNull { it.cosmeticQualifier == cosmeticQualifier }
            ?: checkedError("${account.username} doesn't own a cosmetic item with the qualifier '$cosmeticQualifier'")

        CosmeticsController.update(Filter.new().dragonfly(account.uuid)) {
            it.remove(cosmeticItem).shouldBe(true)?.orError("Could not remove cosmetic item from ${account.username}")
        }

        cosmeticItem.apply {
            enabled = false
            minecraft = null
            if (resetConfig) config = Document()
        }

        CosmeticsController.update(Filter.new().dragonfly(receiverAccount.uuid)) {
            it.add(cosmeticItem)
        }

        success()
    }
}