package core

import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.routing.*
import log

open class Module(val name: String, vararg val routes: ModuleRoute)

fun Routing.enable(module: Module) {
    log("* ${module.name}")
    module.routes.forEach {
        with(it) {
            route("v${version()}") {
                makeRoute(module, it)
            }
            if (it.legacyRoute() != null) {
                route(it.legacyRoute()!!, it.method) {
                    log(" - ${it.method.pretty()} /${it.legacyRoute()!!} (legacy)")
                    handle {
                        handleCall()
                    }
                }
            }
        }
    }
}

private fun Route.makePlainRoute(module: Module, route: ModuleRoute) {
    route(module.name.toLowerCase()) {
        route(route.route, route.method) {
            log(" - ${route.method.pretty()} /v${route.version()}/${module.name.toLowerCase()}/${route.route}")
            handle {
                with(route) { handleCall() }
            }
        }
    }
}

private fun Route.makeRoute(module: Module, route: ModuleRoute) {
    if (route.authentication != null) {
        authenticate(route.authentication, optional = route.optional) {
            makePlainRoute(module, route)
        }
    } else {
        makePlainRoute(module, route)
    }
}

private fun String.fill(amountOfChars: Int): String {
    if (length >= amountOfChars) return this
    return this + " ".repeat(amountOfChars - length)
}

private fun HttpMethod.pretty() = value.toUpperCase().fill(7)
