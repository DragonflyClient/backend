package modules.client

import core.Module
import modules.client.editions.EditionsRoute
import modules.client.editions.EditionsService
import modules.client.files.ClientFilesRoute
import modules.client.files.ClientFilesService

object ClientModule : Module(
    "Client",
    ClientFilesRoute(),
    EditionsRoute()
) {
    val filesService = ClientFilesService()
    val editionsService = EditionsService()
}