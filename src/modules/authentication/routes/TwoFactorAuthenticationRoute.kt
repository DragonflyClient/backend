package modules.authentication.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.TwoFactorAuthentication

class TwoFactorAuthenticationRoute : ModuleRoute("two-factor-authentication", HttpMethod.Post, "jwt", isAuthenticationOptional = true) {

    override suspend fun CallContext.handleCall() {
        val account = getAccount()
        val body = call.receive<JsonObject>()

        when (val action = body.get("action").asString) {
            "enable" -> {
                val code = body.get("code").asString
                TwoFactorAuthentication.enable2FA(account, code)
                json {
                    "success" * true
                    "backup_codes" * account.twoFactorAuthentication.backupCodes
                }
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