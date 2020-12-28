package modules.launcher.editions

import core.MongoDB

class DragonflyEditionsService {
    private val collection = MongoDB.launcherDB.getCollection<DragonflyEdition>("editions")

    suspend fun getEditions(): List<DragonflyEdition> = collection.find().toList()
}