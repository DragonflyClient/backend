package keys.routes.master

import DragonflyBackend
import com.google.cloud.Timestamp
import com.google.cloud.firestore.SetOptions
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import keys.KeyGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Registers the `/keys/generate` route which allows an authenticated master to generate new
 * keys for the client.
 *
 * When starting a get-request to this route, the server will ask for a username and a password
 * if they aren't already set. If the authentication succeeded, it will respond with JSON content
 * that contains the newly generated key and a success boolean.
 */
fun Routing.keysMasterGenerate() {
    authenticate("master") {
        get("/keys/generate") {
            val key = KeyGenerator.generateSafeKey()
            val collection = DragonflyBackend.firestore.collection("keys")
            val document = collection.document(key)
            val timestamp = Timestamp.now()

            withContext(Dispatchers.IO) {
                document.set(
                    mapOf(
                        "attached" to false,
                        "createdOn" to timestamp
                    ), SetOptions.merge()
                ).get()
            }

            call.respond(
                mapOf(
                    "success" to true,
                    "key" to key,
                    "createdOn" to timestamp.toString()
                )
            )
        }
    }
}