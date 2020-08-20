package modules.authentication

import core.Module
import io.ktor.routing.*
import modules.authentication.routes.*

object AuthModule : Module() {

    override fun Routing.provideRouting() {
        routeAuth()
        routeAuthLogin()
        routeAuthRegister()
        routeAuthCookie()
        routeAuthCookieLogin()
        routeAuthCookieRegister()
        routeAuthCookieLogout()
    }
}