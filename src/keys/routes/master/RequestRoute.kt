package keys.routes.master

import DragonflyBackend
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Provides the functionality for the `/keys/request` route that can be used by authenticated masters to
 * request some information about a key.
 *
 * This will check for the existence of the key, whether it is attached and the machine identifier of the
 * device it was attached to. The response will be in JSON format.
 */
fun Routing.routeKeysRequest() {
    authenticate("master") {
        get("/keys/request") {
            val key = call.parameters["key"] ?: call.receiveText()
            val documentReference = DragonflyBackend.firestore.collection("keys").document(key)
            val document = withContext(Dispatchers.IO) { documentReference.get().get() }

            val isExisting = document.exists()
            val isAttached = if (isExisting) document.getBoolean("attached") else null
            val machineIdentifier = if (isExisting) document.getString("machineIdentifier") else null

            call.respond(mutableMapOf<String, Any?>().apply {
                set("success", true)
                set("exists", isExisting)

                if (isExisting) {
                    set("attached", isAttached)

                    if (isAttached == true) {
                        set("machineIdentifier", machineIdentifier)
                    }
                }
            })
        }
    }
}