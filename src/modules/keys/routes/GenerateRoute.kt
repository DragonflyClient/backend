package modules.keys.routes

import core.ModuleRoute
import core.json
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
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
object GenerateRoute : ModuleRoute("generate", HttpMethod.Get, "master") {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        val date = Date()
        val key = KeyGenerator.generateSafeKey()
        val keyDocument = KeyDocument(key, false, date.time, null)

        KeyGenerator.collection.insertOne(keyDocument)

        json {
            "success" * true
            "key" * key
            "createdOn" * date.toLocaleString()
        }
    }

    override fun legacyRoute() = "keys/generate"
}