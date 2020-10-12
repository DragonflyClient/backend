package modules.authentication.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager

class RenameRoute : ModuleRoute("rename", HttpMethod.Post, "jwt", optional = true) {

    private val renameDelay = 1000 * 60 * 60 * 24 * 7

    override suspend fun Call.handleCall() {
        val account = twoWayAuthentication()
        val newUsername = call.receive<JsonObject>().get("name").asString!!
        val renameDate = (account.metadata["renameDate"] as? Long) ?: 0L

        if (System.currentTimeMillis() - renameDate < renameDelay)
            return json(HttpStatusCode.TooManyRequests) {
                "success" * false
                "error" * "You can only change your username every 7 days."
                "next" * renameDate + renameDelay
            }

        AuthenticationManager.assertCanRename(account, newUsername)

        if (newUsername == account.username)
            return success()

        account.username = newUsername
        account.metadata["renameDate"] = System.currentTimeMillis()
        account.save()

        success()
    }
}