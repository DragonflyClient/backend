package modules.minecraft.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.JwtConfig
import modules.minecraft.util.MinecraftLinkManager
import modules.minecraft.util.MinecraftLinkManager.getByMinecraftUUID

object CookieUnlinkRoute : ModuleRoute("cookie/unlink", HttpMethod.Post) {

    override suspend fun CallContext.handleCall() {
        val cookie = call.request.cookies["dragonfly-token"] ?: checkedError("No token cookie found")
        val token = JwtConfig.verifier.verify(cookie)
        val account = token.getClaim("uuid").asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }
            ?: checkedError("Not authenticated with Dragonfly")
        val uuid = MinecraftLinkManager.parseWithoutDashes(call.receiveText())

        if (getByMinecraftUUID(uuid) == account) {
            MinecraftLinkManager.unlink(account, uuid)
            success()
        } else checkedError("This account is not linked to your Dragonfly account")
    }
}