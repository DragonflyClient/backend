package modules.keys.routes

import core.ModuleRoute
import core.json
import io.ktor.routing.*
import modules.keys.util.getKeyDocument
import modules.keys.util.receiveParameters

/**
 * Provides the `/keys/validate` route that is called on every client startup to validate
 * the currently saved key by comparing the machine identifiers.
 *
 * Just like [Routing.routeKeysAttach], this function has to provide the [KeyMachineParameters] in
 * JSON format. It will then check if the key is still valid and compare the current machine id
 * to the stored one.
 */
object ValidateRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        post("/keys/validate") {
            val parameters = receiveParameters()
            val machineIdentifier = parameters.machineIdentifier
            val keyDocument = getKeyDocument(parameters)

            if (!keyDocument.attached) {
                return@post json {
                    "success" * false
                    "message" * "The provided key isn't attached to any device!"
                }
            }

            if (keyDocument.machineIdentifier != machineIdentifier) {
                return@post json {
                    "success" * false
                    "message" * "The provided key is attached to another device!"
                }
            }

            json {
                "success" * true
                "message" * "success"
            }
        }
    }
}