package modules.version

import core.Module
import modules.version.routes.*

object VersionModule : Module(
    "Version",
    VersionRoute,
    PublishRoute,
    UpdatesHistoryRoute,
    UpdatesRoute,
    VersionInstallerRoute
)