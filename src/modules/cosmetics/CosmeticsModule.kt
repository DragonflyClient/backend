package modules.cosmetics

import core.Module
import modules.cosmetics.routes.FindRoute

object CosmeticsModule : Module(
    "Cosmetics",
    FindRoute
)