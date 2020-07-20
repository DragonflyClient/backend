package auth.routes

import auth.Authentication
import auth.JwtConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.routeAuth() {
    post("auth") {
        val credentials = call.receive<UserPasswordCredential>()
        val account = Authentication.verify(credentials.name, credentials.password)
            ?: return@post call.respond(mapOf(
                "success" to false,
                "error" to "Invalid username or password"
            ))
        val token = JwtConfig.makeToken(account)
        call.respond(mapOf(
            "token" to token
        ))
    }
}