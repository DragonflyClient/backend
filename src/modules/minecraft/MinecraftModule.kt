package modules.minecraft

import core.Module
import modules.minecraft.routes.*

object MinecraftModule : Module(
    "Minecraft",
    LinkRoute,
    UnlinkRoute,
    CookieUnlinkRoute
)