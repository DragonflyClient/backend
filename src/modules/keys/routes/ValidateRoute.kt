package modules.keys.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.keys.*

/**
 * Provides the `/keys/validate` route that is called on every client startup to validate
 * the currently saved key by comparing the machine identifiers.
 *
 * Just like [Routing.routeKeysAttach], this function has to provide the [KeyMachineParameters] in
 * JSON format. It will then check if the key is still valid and compare the current machine id
 * to the stored one.
 */
fun Routing.routeKeysValidate() {
    post("/keys/validate") {
        val parameters = receiveParameters()
        val machineIdentifier = parameters.machineIdentifier
        val keyDocument = getKeyDocument(parameters)

        if (!keyDocument.attached) {
            return@post call.respond(mapOf(
                "success" to false,
                "message" to "The provided key isn't attached to any device!"
            ))
        }

        if (keyDocument.machineIdentifier != machineIdentifier) {
            return@post call.respond(mapOf(
                "success" to false,
                "message" to "The provided key is attached to another device!"
            ))
        }

        call.respond(mapOf(
            "success" to true,
            "message" to "success"
        ))
    }
}
