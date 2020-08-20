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
 * Creates a /register route to create new accounts.
 */
object RegisterRoute : ModuleRoute("register", HttpMethod.Post) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            val account = Authentication.register(credentials.name, credentials.password)
            val token = JwtConfig.makeToken(account)

            json {
                "success" * true
                "token" * token
                "identifier" * account.identifier
                "username" * account.username
                "creationDate" * account.creationDate
                "permissionLevel" * account.permissionLevel
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}