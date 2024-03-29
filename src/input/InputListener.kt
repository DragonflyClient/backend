package input

import core.Debugger
import log
import modules.client.ClientModule
import modules.version.util.VersionManager
import modules.version.util.update.UpdateHistory
import java.util.*
import kotlin.concurrent.thread

object InputListener {

    /**
     * Starts listening for console input and catches errors during the parsing and listening
     * process of the scanner.
     */
    fun startListening() = thread {
        val scanner = Scanner(System.`in`)

        try {
            while (scanner.hasNext()) {
                try {
                    val line = scanner.nextLine()
                    handleInput(line)
                } catch (e: Exception) {
                    log("Could not parse input from console scanner: ${e.message}")
                }
            }
        } catch (e: Exception) {
            log("Console scanner stopped listening: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Handles the given user [input] from the command line by checking for existing
     * commands.
     */
    private fun handleInput(input: String) {
        when (input) {
            "publish" -> {
                VersionManager.reloadJsonObject()
                UpdateHistory.reloadJsonObject()
            }
            "debug" -> Debugger.toggle()
            "launcher-files" -> ClientModule.filesService.loadFiles()
        }
    }
}
