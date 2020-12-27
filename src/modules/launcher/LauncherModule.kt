package modules.launcher

import core.Module
import modules.launcher.files.LauncherFilesRoute
import modules.launcher.files.LauncherFilesService

object LauncherModule : Module(
    "Launcher",
    LauncherFilesRoute()
) {
    val filesService = LauncherFilesService()
}