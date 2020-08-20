package modules.keys.routes

import core.ModuleRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
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
object RequestRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        authenticate("master") {
            get("/keys/request") {
                val key = call.parameters["key"] ?: error("Missing URL parameter 'key'")
                val keyDocument = KeyGenerator.collection.findOne(KeyDocument::key eq key)
                val machineIdentifier = keyDocument?.machineIdentifier

                call.respond(mutableMapOf<String, Any?>().apply {
                    set("success", true)
                    set("exists", keyDocument != null)

                    if (keyDocument != null) {
                        set("attached", keyDocument.attached)
                        set("createdOn", Date(keyDocument.createdOn).toLocaleString())

                        if (keyDocument.attached) {
                            set("machineIdentifier", machineIdentifier)
                        }
                    }
                })
            }
        }
    }
}