package modules.launcher

import core.Module
import modules.launcher.editions.DragonflyEditionsRoute
import modules.launcher.editions.DragonflyEditionsService
import modules.launcher.files.LauncherFilesRoute
import modules.launcher.files.LauncherFilesService

object LauncherModule : Module(
    "Launcher",
    LauncherFilesRoute(),
    DragonflyEditionsRoute()
) {
    val filesService = LauncherFilesService()
    val editionsService = DragonflyEditionsService()
}