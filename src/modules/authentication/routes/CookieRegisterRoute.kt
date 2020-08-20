package modules.authentication.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.date.*
import modules.authentication.util.Authentication
import modules.authentication.util.JwtConfig

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
fun Routing.routeAuthCookieRegister() {
    post("/cookie/register") {
        val credentials = call.receive<UserPasswordCredential>()
        val account = Authentication.register(credentials.name, credentials.password)
        val token = JwtConfig.makeToken(account)

        call.response.cookies.append(Cookie(
            name = "dragonfly-token",
            value = token,
            httpOnly = true,
            secure = true,
            expires = GMTDate(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 30L)), // 30 days
            domain = "playdragonfly.net",
            path = "/",
            extensions = mapOf(
                "SameSite" to "Strict"
            )
        ))

        call.respond(mapOf(
            "success" to true,
            "identifier" to account.identifier,
            "username" to account.username,
            "creationDate" to account.creationDate,
            "permissionLevel" to account.permissionLevel
        ))
    }
}
