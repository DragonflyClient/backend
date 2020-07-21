package version

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

/**
 * Reads the version from the dragonfly-version.json file in the running directory.
 */
object VersionManager {

    /**
     * The file that contains the information about the Dragonfly version in JSON format
     */
    private val file = File("dragonfly-version.json")

    /**
     * The json object parsed from the content of the [file]
     */
    private var jsonObject = readFile()

    /**
     * Json content for the stable channel
     */
    val stable: JsonObject
        get() = jsonObject["stable"].asJsonObject

    /**
     * Json content for the early access channel
     */
    val earlyAccess: JsonObject
        get() = jsonObject["earlyAccess"].asJsonObject

    /**
     * The version of the Dragonfly Installer
     */
    val installer: String
        get() = jsonObject["installer"].asString

    /**
     * Reloads the [jsonObject] from the [file]
     */
    fun reloadJsonObject() {
        jsonObject = readFile()
        println("Reloaded JSON object!")
    }

    /**
     * Reads the [file] and parses its content to a json object
     */
    private fun readFile(): JsonObject = file.reader().use { JsonParser().parse(it) }.asJsonObject
}