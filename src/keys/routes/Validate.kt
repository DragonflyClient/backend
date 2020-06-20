package keys.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import keys.*
import org.slf4j.Logger
import org.slf4j.impl.StaticLoggerBinder

/**
 * Provides the `/keys/validate` route that is called on every client startup to validate
 * the currently saved key by comparing the machine identifiers.
 *
 * Just like [Routing.keysAttach], this function has to provide the [KeyMachineParameters] in
 * JSON format. It will then check if the key is still valid and compare the current machine id
 * to the stored one.
 */
fun Routing.keysValidate() {
    get("/keys/validate") {
        tryReceiveKeyMachineParameters()?.run {
            println("Hello")
            if (documentSnapshot!!.getBoolean("attached") != true) {
                return@get call.respond(mapOf(
                    "success" to false,
                    "message" to "The provided key isn't attached to any device!"
                ))
            }

            if (documentSnapshot!!.getString("machineIdentifier") != machineIdentifier) {
                return@get call.respond(mapOf(
                    "success" to false,
                    "message" to "The provided key is attached to another device!"
                ))
            }

            call.respond(mapOf(
                "success" to true
            ))
        }
    }
}