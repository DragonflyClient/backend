package modules.minecraft.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.Account
import modules.minecraft.util.MinecraftLinkManager
import modules.minecraft.util.MinecraftLinkManager.getByMinecraftUUID
import modules.minecraft.util.MinecraftLinkManager.verifyAccount

object LinkRoute : ModuleRoute("link", HttpMethod.Post, "jwt") {

    override suspend fun Call.handleCall() {
        val account = call.authentication.principal<Account>() ?: fatal("Not authenticated with Dragonfly")
        val token = call.receiveText()
        val uuid = verifyAccount(token) ?: fatal("Invalid Minecraft access token")

        if (getByMinecraftUUID(uuid) == null) {
            MinecraftLinkManager.link(account, uuid)
            success()
        } else fatal("This account is already linked to a Dragonfly account")
    }
}