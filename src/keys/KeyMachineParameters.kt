package keys

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A simple data class containing parameters representing a key and the corresponding machine.
 *
 * Additionally, when using the [tryReceiveKeyMachineParameters] function, the [documentSnapshot] and
 * [documentReference] for the Cloud Firestore database will be loaded.
 *
 * @param key the key, mostly generated via [KeyGenerator.generateKey]
 * @param machineIdentifier a string clearly identifying the machine
 *
 * @property documentReference the reference to the Firestore document (used for setting values)
 * @property documentSnapshot the snapshot of the Firestore document (used for getting values)
 */
data class KeyMachineParameters(val key: String, val machineIdentifier: String) {
    var documentReference: DocumentReference? = null
    var documentSnapshot: DocumentSnapshot? = null
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
            "error" to "INVALID_BODY",
            "errorMessage" to "Could not parse parameters from request body, probably bad request!"
        ))
        return null
    }

    val documentReference = DragonflyBackend.firestore.collection("keys").document(parameters.key)
    val document = withContext(Dispatchers.IO) { documentReference.get().get() }

    if (!document.exists()) {
        call.respond(mapOf(
            "success" to false,
            "error" to "KEY_NOT_FOUND",
            "errorMessage" to "The provided key does not exist!"
        ))
        return null
    }

    return parameters.also {
        it.documentSnapshot = document
        it.documentReference = documentReference
    }
}