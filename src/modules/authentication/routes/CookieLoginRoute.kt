package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.TwoFactorAuthentication
import modules.authentication.util.data.LoginData

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object CookieLoginRoute : ModuleRoute("cookie/login", HttpMethod.Post) {

    override suspend fun CallContext.handleCall() {
        val credentials = call.receive<LoginData>()
        val account = AuthenticationManager.verify(credentials.name, credentials.password)
            ?: checkedError("Invalid username or password")

        if (account.twoFactorAuthentication.enabled) {
            if (credentials.code == null) return json {
                "success" * false
                "require2FA" * true
                "error" * "Please provide a two factor authentication code"
            }
            if (!TwoFactorAuthentication.verifyCode(account, credentials.code)) checkedError("Invalid two factor authentication code")
        }

        respondToken(account)
    }
}
