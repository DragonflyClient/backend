package modules.client.editions

import core.MongoDB

class EditionsService {
    private val collection = MongoDB.launcherDB.getCollection<DragonflyEdition>("editions")

    suspend fun getEditions(): List<DragonflyEdition> = collection.find().toList()
}