package modules.cosmetics

import core.Module
import modules.cosmetics.routes.*

object CosmeticsModule : Module(
    "Cosmetics",
    FindRoute,
    AvailableRoute,
    ToggleRoute,
    BindRoute,
    ConfigureRoute
)