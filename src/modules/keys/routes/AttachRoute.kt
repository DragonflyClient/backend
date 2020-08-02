package modules.keys.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.keys.*
import org.litote.kmongo.coroutine.updateOne

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
    post("/keys/attach") {
        tryReceiveKeyMachineParameters()?.run {
            if (keyDocument!!.attached) {
                return@post call.respond(mapOf(
                    "success" to false,
                    "message" to "The provided key is already attached to a device!"
                ))
            }

            keyDocument!!.attached = true
            keyDocument!!.machineIdentifier = machineIdentifier

            KeyGenerator.collection.updateOne(keyDocument!!)

            call.respond(mapOf(
                "success" to true,
                "message" to "success"
            ))
        }
    }
}
