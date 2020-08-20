package core

import io.ktor.routing.*
import modules.auth.AuthModule.provideRouting

abstract class Module {

    abstract fun Routing.provideRouting()
}

fun Routing.enable(module: Module) = with(this) { provideRouting() }