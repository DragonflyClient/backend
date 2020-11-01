package modules.authentication.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager
import modules.community.notifications.NotificationsManager.sendNotification
import modules.community.profile.ProfileManager.getProfile

class RenameRoute : ModuleRoute("rename", HttpMethod.Post, "jwt", isAuthenticationOptional = true) {

    private val renameDelay = 1000L * 60L * 60L * 24L * 7L

    override suspend fun CallContext.handleCall() {
        val account = requireAccount()
        val newUsername = call.receive<JsonObject>().get("name").asString!!
        val renameDate = (account.metadata["renameDate"] as? Long) ?: 0L

        if (System.currentTimeMillis() - renameDate < renameDelay)
            return json(HttpStatusCode.TooManyRequests) {
                "success" * false
                "error" * "You can only change your username every 7 days."
                "next" * (renameDate + renameDelay)
            }

        if (newUsername == account.username)
            checkedError("Your account is already named like that!")

        AuthenticationManager.assertCanRename(account, newUsername)

        account.username = newUsername
        account.metadata["renameDate"] = System.currentTimeMillis()
        account.save()

        account.getProfile().sendNotification("Account", "Your username has been changed to **$newUsername**.", "pencil")

        success()
    }
}