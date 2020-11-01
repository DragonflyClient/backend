package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager
import modules.community.notifications.NotificationsManager.sendNotification
import modules.community.profile.ProfileManager.getProfile
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter
import org.bson.Document

object TransferRoute : ModuleRoute("transfer", HttpMethod.Post, "jwt", true) {

    private const val transferDelay = 1000 * 60 * 60 * 24 * 2

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

        if (cosmeticItem.lastTransferred != null && System.currentTimeMillis() - cosmeticItem.lastTransferred!! < transferDelay) {
            val remaining = cosmeticItem.lastTransferred!! + transferDelay - System.currentTimeMillis()
            val minutes = (remaining / (1000 * 60)) % 60
            val hours = remaining / (1000 * 60 * 60)
            val formatted = if (hours > 0) "$hours hours and $minutes minutes" else "$minutes minutes"
            checkedError("This cosmetic has just been transferred and can be moved again in $formatted")
        }

        CosmeticsController.update(Filter.new().dragonfly(account.uuid)) {
            it.remove(cosmeticItem).shouldBe(true)?.orError("Could not remove cosmetic item from ${account.username}")
        }

        cosmeticItem.apply {
            enabled = false
            minecraft = null
            lastTransferred = System.currentTimeMillis()
            if (resetConfig) config = Document()
        }

        CosmeticsController.update(Filter.new().dragonfly(receiverAccount.uuid)) {
            it.add(cosmeticItem)
        }

        val cosmeticName = CosmeticsController.getAvailableById(cosmeticItem.cosmeticId)!!.getString("name")
        account.getProfile().sendNotification("Cosmetics", "**${account.username}** gifted you **$cosmeticName**.", "gift")

        success()
    }
}