package modules.minecraft.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.models.Account
import modules.community.notifications.NotificationAction
import modules.community.notifications.NotificationsManager.sendNotification
import modules.community.profile.ProfileManager.getProfile
import modules.minecraft.util.MinecraftLinkManager
import modules.minecraft.util.MinecraftLinkManager.getByMinecraftUUID
import modules.minecraft.util.MinecraftLinkManager.verifyAccount

object LinkRoute : ModuleRoute("link", HttpMethod.Post, "jwt") {

    override suspend fun CallContext.handleCall() {
        val account = call.authentication.principal<Account>() ?: checkedError("Not authenticated with Dragonfly")
        val token = call.receiveText()
        val (name, uuid) = verifyAccount(token) ?: checkedError("Invalid Minecraft access token")

        if (getByMinecraftUUID(uuid) == null) {
            MinecraftLinkManager.link(account, uuid)
            account.getProfile().sendNotification(
                "Account", "The Minecraft account **$name** has been linked to your Dragonfly account.", "link",
                NotificationAction.openUrl("https://dashboard.playdragonfly.net/account")
            )
            success()
        } else checkedError("This account is already linked to a Dragonfly account")
    }
}