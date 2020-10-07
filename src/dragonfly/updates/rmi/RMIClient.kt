package dragonfly.updates.rmi

import log
import modules.version.util.update.Update
import secrets.KEYS_MASTER_PASSWORD
import java.rmi.registry.LocateRegistry

object RMIClient {

    fun invokePublishUpdate(
        update: Update,
        earlyAccess: Boolean,
        stable: Boolean
    ) {
        log("Invoking publish-update using RMI...")
        val registry = LocateRegistry.getRegistry()
        val service: UpdateService = registry.lookup("UpdateService") as UpdateService
        log("Using service $service...")

        try {
            service.publishUpdate(
                update.version, update.title, update.patchNotes, update.releaseDate,
                earlyAccess, stable, "master", KEYS_MASTER_PASSWORD
            )
            log("Method invoked!")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
