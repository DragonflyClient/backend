package modules.authentication.routes

import core.ModuleRoute
import core.respondAccount
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import modules.authentication.util.Account

object AuthRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        authenticate("jwt", optional = true) {
            post("/auth") {
                val account = call.authentication.principal<Account>()

                respondAccount(account)
            }
        }
    }
}