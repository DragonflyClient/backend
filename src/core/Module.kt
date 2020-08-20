package core

import io.ktor.routing.*

open class Module(val name: String, vararg val routes: ModuleRoute)

fun Routing.enable(module: Module) {
    module.routes.forEach {
        with(it) {
            provideRoute()
        }
    }
}