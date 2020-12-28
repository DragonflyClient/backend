package modules.client

import core.Module
import modules.client.announcements.AnnouncementsRoute
import modules.client.announcements.AnnouncementsService
import modules.client.editions.EditionsRoute
import modules.client.editions.EditionsService
import modules.client.files.ClientFilesRoute
import modules.client.files.ClientFilesService

object ClientModule : Module(
    "Client",
    ClientFilesRoute(),
    EditionsRoute(),
    AnnouncementsRoute()
) {
    val filesService = ClientFilesService()
    val editionsService = EditionsService()
    val announcementsService = AnnouncementsService()
}