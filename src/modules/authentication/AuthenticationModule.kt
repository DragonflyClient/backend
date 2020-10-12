package modules.authentication

import core.Module
import modules.authentication.routes.*

object AuthenticationModule : Module(
    "Authentication",
    TokenRoute,
    LoginRoute,
    RegisterRoute,
    CookieTokenRoute,
    CookieLoginRoute,
    CookieLogoutRoute,
    TwoFactorAuthenticationRoute(),
    RenameRoute()
)