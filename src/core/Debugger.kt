package core

import log

object Debugger {
    var isDebuggingEnabled = false
        private set

    fun toggle() {
        isDebuggingEnabled = !isDebuggingEnabled
        log("Debugging has been ${if (isDebuggingEnabled) "enabled" else "disabled"}.")
    }
}