package core

import io.ktor.auth.*
import io.ktor.routing.*

open class Module(val name: String, vararg val routes: ModuleRoute)

fun Routing.enable(module: Module) {
    module.routes.forEach {
        with(it) {
            route("v${version()}") {
                if (authentication != null) {
                    authenticate(authentication, optional = optional) {
                        route(module.name.toLowerCase()) {
                            route(route, method) {
                                handle { handleCall() }
                            }
                        }

                        if (legacyRoute() != null) {
                            route(legacyRoute()!!) {
                                handle { handleCall() }
                            }
                        }
                    }
                } else {
                    route(module.name.toLowerCase()) {
                        route(route, method) {
                            handle { handleCall() }
                        }
                    }

                    if (legacyRoute() != null) {
                        route(legacyRoute()!!) {
                            handle { handleCall() }
                        }
                    }
                }
            }
        }
    }
}