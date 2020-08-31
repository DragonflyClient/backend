package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter

object ToggleRoute : ModuleRoute("toggle", HttpMethod.Post, "jwt", true) {

    override suspend fun Call.handleCall() {
        var account = call.authentication.principal<Account>()
        val body = call.receive<JsonObject>()
        val cosmeticQualifier = body["cosmeticQualifier"].asString
        val enable = body["enable"].asBoolean

        if (account == null) {
            val cookie = call.request.cookies["dragonfly-token"] ?: error("Unauthenticated")
            val token = JwtConfig.verifier.verify(cookie)
            account = token.getClaim("uuid").asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }
        }

        if (account == null) {
            return json {
                "success" * false
                "error" * "Unauthenticated"
            }
        }

        var found = false
        CosmeticsController.updateEach(Filter.new().dragonfly(account.uuid)) {
            if (it.cosmeticQualifier == cosmeticQualifier) {
                it.enabled = enable
                found = true
            }
        }

        if (found) {
            success()
        } else {
            error("Invalid cosmetic qualifier")
        }
    }
}