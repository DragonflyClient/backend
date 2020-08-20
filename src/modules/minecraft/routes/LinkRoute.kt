package modules.minecraft.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.Account
import modules.minecraft.util.MinecraftLinkManager
import modules.minecraft.util.MinecraftLinkManager.getByMojangUUID
import modules.minecraft.util.MinecraftLinkManager.verifyAccount

object LinkRoute : ModuleRoute("link", HttpMethod.Post, "jwt") {

    override suspend fun Call.handleCall() {
        val account = call.authentication.principal<Account>() ?: error("Not authenticated with Dragonfly")
        val token = call.receiveText()
        val uuid = verifyAccount(token) ?: error("Invalid Minecraft access token")

        if (getByMojangUUID(uuid) == null) {
            MinecraftLinkManager.link(account, uuid)
            success()
        } else error("This account is already linked to a Dragonfly account")
    }
}