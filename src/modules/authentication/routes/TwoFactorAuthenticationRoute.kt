package modules.authentication.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.TwoFactorAuthentication

class TwoFactorAuthenticationRoute : ModuleRoute("two-factor-authentication", HttpMethod.Post, "jwt", optional = true) {

    override suspend fun Call.handleCall() {
        val account = twoWayAuthentication()
        val body = call.receive<JsonObject>()

        when (val action = body.get("action").asString) {
            "enable" -> {
                val code = body.get("code").asString
                TwoFactorAuthentication.enable2FA(account, code)
                success()
            }
            "disable" -> {
                TwoFactorAuthentication.disable2FA(account)
                success()
            }
            "request" -> {
                TwoFactorAuthentication.request2FA(account)
                json {
                    "success" * true
                    "qr_code" * TwoFactorAuthentication.generateQRCode(account)
                }
            }
            else -> checkedError("Invalid action '$action'")
        }
    }
}