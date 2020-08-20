package modules.keys.routes

import core.ModuleRoute
import core.json
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import modules.keys.util.KeyDocument
import modules.keys.util.KeyGenerator
import org.litote.kmongo.eq
import java.util.*

/**
 * Provides the functionality for the `/keys/request` route that can be used by authenticated masters to
 * request some information about a key.
 *
 * This will check for the existence of the key, whether it is attached and the machine identifier of the
 * device it was attached to. The response will be in JSON format.
 */
object RequestRoute : ModuleRoute("request", HttpMethod.Get, "master") {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        val key = call.parameters["key"] ?: error("Missing URL parameter 'key'")
        val keyDocument = KeyGenerator.collection.findOne(KeyDocument::key eq key)
        val machineIdentifier = keyDocument?.machineIdentifier

        json {
            "success" * true
            "exists" * (keyDocument != null)

            if (keyDocument != null) {
                "attached" * keyDocument.attached
                "createdOn" * Date(keyDocument.createdOn).toLocaleString()

                if (keyDocument.attached) {
                    "machineIdentifier" * machineIdentifier
                }
            }
        }
    }
}