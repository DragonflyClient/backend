package modules.keys

import core.Module
import io.ktor.routing.*
import modules.keys.routes.*

object KeysModule : Module() {

    override fun Routing.provideRouting() {
        routeKeysGenerate()
        routeKeysRequest()
        routeKeysAttach()
        routeKeysValidate()
    }
}