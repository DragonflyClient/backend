package modules.keys.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.keys.util.KeyDocument
import modules.keys.util.KeyGenerator
import java.util.*

/**
 * Registers the `/keys/generate` route which allows an authenticated master to generate new
 * keys for the client.
 *
 * When starting a get-request to this route, the server will ask for a username and a password
 * if they aren't already set. If the authentication succeeded, it will respond with JSON content
 * that contains the newly generated key and a success boolean.
 */
fun Routing.routeKeysGenerate() {
    authenticate("master") {
        get("/keys/generate") {
            val date = Date()
            val key = KeyGenerator.generateSafeKey()
            val keyDocument = KeyDocument(
                key, false, date.time, null
            )

            KeyGenerator.collection.insertOne(keyDocument)

            call.respond(
                mapOf(
                    "success" to true,
                    "key" to key,
                    "createdOn" to date.toLocaleString()
                )
            )
        }
    }
}
