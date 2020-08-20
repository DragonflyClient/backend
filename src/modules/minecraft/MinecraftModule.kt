package modules.minecraft

import core.Module
import modules.minecraft.routes.LinkRoute
import modules.minecraft.routes.UnlinkRoute

object MinecraftModule : Module(
    "Minecraft",
    LinkRoute,
    UnlinkRoute
)