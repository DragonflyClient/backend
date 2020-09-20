package modules.minecraft.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.Account
import modules.minecraft.util.MinecraftLinkManager
import modules.minecraft.util.MinecraftLinkManager.getByMinecraftUUID

object UnlinkRoute : ModuleRoute("unlink", HttpMethod.Post, "jwt") {

    override suspend fun Call.handleCall() {
        val account = call.authentication.principal<Account>() ?: checkedError("Not authenticated with Dragonfly")
        val uuid = MinecraftLinkManager.parseWithoutDashes(call.receiveText())

        if (getByMinecraftUUID(uuid) == account) {
            MinecraftLinkManager.unlink(account, uuid)
            success()
        } else checkedError("This account is not linked to a Dragonfly account")
    }
}