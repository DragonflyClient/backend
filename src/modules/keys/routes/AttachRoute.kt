package modules.keys.routes

import com.google.cloud.firestore.SetOptions
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.keys.KeyMachineParameters
import modules.keys.tryReceiveKeyMachineParameters

/**
 * Creates the `/keys/attach` route that will attach a specific device to a key and thus
 * makes it unusable for other devices (with different machine identifiers).
 *
 * The body of the request must contain the [KeyMachineParameters] in JSON format (i.e. the
 * `key` and the `machineIdentifier`). If the attaching process was a success, the response
 * will hold a `success` boolean set to true, otherwise, an additional message will be provided
 * that can be displayed in the client.
 */
fun Routing.routeKeysAttach() {
    post("/modules/keys/attach") {
        tryReceiveKeyMachineParameters()?.run {
            if (documentSnapshot!!.getBoolean("attached") == true) {
                return@post call.respond(mapOf(
                    "success" to false,
                    "message" to "The provided key is already attached to a device!"
                ))
            }

            documentReference!!.set(
                mapOf(
                    "attached" to true,
                    "machineIdentifier" to machineIdentifier
                ), SetOptions.merge()
            )

            call.respond(mapOf(
                "success" to true,
                "message" to "success"
            ))
        }
    }
}