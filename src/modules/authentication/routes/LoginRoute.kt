package modules.authentication.routes

import core.ModuleRoute
import core.json
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import modules.authentication.util.Authentication
import modules.authentication.util.JwtConfig

/**
 * Creates a /login route that verifies the credentials and returns a JWT for authenticating
 * with the account.
 */
object LoginRoute : ModuleRoute("login", HttpMethod.Post) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        val credentials = call.receive<UserPasswordCredential>()
        val account = Authentication.verify(credentials.name, credentials.password)
            ?: return call.respond(mapOf(
                "success" to false,
                "error" to "Invalid username or password"
            ))
        val token = JwtConfig.makeToken(account)

        json {
            "success" * true
            "identifier" * account.identifier
            "username" * account.username
            "creationDate" * account.creationDate
            "permissionLevel" * account.permissionLevel
            "token" * token
        }
    }
}