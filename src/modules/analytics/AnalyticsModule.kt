package modules.analytics

import core.Module
import modules.analytics.routes.DownloadCountRoute

object AnalyticsModule : Module(
    "Analytics",
    DownloadCountRoute()
)
