package modules.authentication.routes

import core.ModuleRoute
import core.respondAccount
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import modules.authentication.util.Account

object TokenRoute : ModuleRoute("token", HttpMethod.Post, "jwt", true) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        val account = call.authentication.principal<Account>()

        respondAccount(account)
    }
}