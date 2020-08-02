package modules.keys

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.litote.kmongo.eq

/**
 * A simple data class containing parameters representing a key and the corresponding machine.
 *
 * Additionally, when using the [tryReceiveKeyMachineParameters] function, the [keyDocument]
 * will be loaded from the database.
 *
 * @param key the key, mostly generated via [KeyGenerator.generateKey]
 * @param machineIdentifier a string clearly identifying the machine
 *
 * @property keyDocument the document that represents the key in the database
 */
data class KeyMachineParameters(val key: String, val machineIdentifier: String) {
    var keyDocument: KeyDocument? = null
}

/**
 * A convenient function to receive the [KeyMachineParameters] from the request body and send
 * custom error responses if they are invalid or if the key does't exist.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.tryReceiveKeyMachineParameters(): KeyMachineParameters? {
    val parameters = try {
        call.receive<KeyMachineParameters>()
    } catch (e: Throwable) {
        call.respond(mapOf(
            "success" to false,
            "message" to "Could not parse parameters from request body, probably bad request!"
        ))
        return null
    }

    val keyDocument = KeyGenerator.collection.findOne(KeyDocument::key eq parameters.key)
        ?: return call.respond(mapOf(
            "success" to false,
            "message" to "The provided key does not exist!"
        )).run { null }

    return parameters.also {
        it.keyDocument = keyDocument
    }
}
