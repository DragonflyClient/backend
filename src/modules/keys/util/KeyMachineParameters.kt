package modules.keys.util

import core.CallContext
import core.checkedError
import io.ktor.application.*
import io.ktor.request.*
import org.litote.kmongo.eq

/**
 * A simple data class containing parameters representing a key and the corresponding machine.
 *
 * @param key the key, mostly generated via [KeyGenerator.generateKey]
 * @param machineIdentifier a string clearly identifying the machine
 */
data class KeyMachineParameters(val key: String, val machineIdentifier: String)

/**
 * A convenient function to receive the [KeyMachineParameters] from the request body and send
 * custom error responses if they are invalid or if the key doesn't exist.
 */
suspend fun CallContext.receiveParameters(): KeyMachineParameters {
    return call.receive()
}

/**
 * Returns the key document for the given [parameters] or throws an error if the provided key doesn't exist.
 */
suspend fun getKeyDocument(parameters: KeyMachineParameters): KeyDocument =
    KeyGenerator.collection.findOne(KeyDocument::key eq parameters.key)
        ?: checkedError("The provided key does not exist!")
