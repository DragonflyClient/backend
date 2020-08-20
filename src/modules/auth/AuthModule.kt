package modules.auth

import core.Module
import io.ktor.routing.*
import modules.auth.routes.*

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