package dragonfly.updates.rmi

import kotlinx.coroutines.*
import log
import modules.version.update.Update
import secrets.KEYS_MASTER_PASSWORD
import java.rmi.registry.LocateRegistry

object RMIClient {

    suspend fun invokePublishUpdate(
        update: Update,
        earlyAccess: Boolean,
        stable: Boolean
    ) {
        log("Invoking publish-update using RMI...")
        coroutineScope {
            launch(Dispatchers.IO) {
                val registry = LocateRegistry.getRegistry()
                val service: UpdateService = registry.lookup("UpdateService") as UpdateService
                log("Using service $service...")

                service.publishUpdate(
                    update.version,
                    update.title,
                    update.patchNotes,
                    update.releaseDate,
                    earlyAccess,
                    stable,
                    "master",
                    KEYS_MASTER_PASSWORD
                )
                log("Method invoked!")
            }
        }
    }
}
