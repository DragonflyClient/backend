package modules.community

import core.Module
import modules.community.notifications.NotificationsRoute

object CommunityModule : Module(
    "Community",
    NotificationsRoute()
)