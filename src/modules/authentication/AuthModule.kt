package modules.authentication

import core.Module
import modules.authentication.routes.*

object AuthModule : Module(
    "Auth",
    AuthRoute,
    LoginRoute,
    RegisterRoute,
    CookieAuthRoute,
    CookieLoginRoute,
    CookieLogoutRoute,
    CookieRegisterRoute
)