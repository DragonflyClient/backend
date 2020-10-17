package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.*
import modules.authentication.util.data.LoginData

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object LoginRoute : ModuleRoute("login", HttpMethod.Post) {

    override suspend fun CallContext.handleCall() {
        val credentials = call.receive<LoginData>()
        val account = AuthenticationManager.verify(credentials.name, credentials.password)
            ?: checkedError("Invalid username or password")

        if (account.twoFactorAuthentication.enabled) {
            if (credentials.code == null) return json {
                "success" * false
                "require2FA" * true
            }
            if (TwoFactorAuthentication.verifyCode(account, credentials.code)) checkedError("Invalid 2FA code")
        }

        val token = JwtConfig.makeToken(account)

        json {
            "success" * true
            "token" * token
            +account
        }
    }
}