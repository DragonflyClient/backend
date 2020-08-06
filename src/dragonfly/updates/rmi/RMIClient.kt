package dragonfly.updates.rmi

import kotlinx.coroutines.*
import modules.version.update.Update
import secrets.KEYS_MASTER_PASSWORD
import java.rmi.registry.LocateRegistry

object RMIClient {

    suspend fun invokePublishUpdate(
        update: Update,
        earlyAccess: Boolean,
        stable: Boolean
    ) {
        coroutineScope {
            launch(Dispatchers.IO) {
                val registry = LocateRegistry.getRegistry()
                val server: UpdateService = registry.lookup("UpdateService") as UpdateService

                server.publishUpdate(
                    update.version,
                    update.title,
                    update.patchNotes,
                    update.releaseDate,
                    earlyAccess,
                    stable,
                    "master",
                    KEYS_MASTER_PASSWORD
                )
            }
        }
    }
}
