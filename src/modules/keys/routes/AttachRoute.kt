package modules.keys.routes

import core.*
import io.ktor.http.*
import modules.keys.util.*
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
object AttachRoute : ModuleRoute("attach", HttpMethod.Post) {

    override suspend fun CallContext.handleCall() {
        val parameters = receiveParameters()
        val machineIdentifier = parameters.machineIdentifier
        val keyDocument = getKeyDocument(parameters)

        if (keyDocument.attached) {
            return json {
                "success" * false
                "message" * "The provided key is already attached to a device!"
            }
        }

        keyDocument.attached = true
        keyDocument.machineIdentifier = machineIdentifier

        KeyGenerator.collection.updateOne(keyDocument)

        json {
            "success" * true
            "message" * "success"
        }
    }
}