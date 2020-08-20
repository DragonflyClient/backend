package modules.authentication.routes

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import modules.authentication.util.Account

object TokenRoute : ModuleRoute("token", HttpMethod.Post, "jwt", true) {

    override suspend fun Call.handleCall() {
        val account = call.authentication.principal<Account>()

        respondAccount(account)
    }

    override fun legacyRoute() = "auth"
}