package version

import com.google.gson.*
import java.io.File
import kotlin.reflect.KProperty

/**
 * Reads the version from the dragonfly-version.json file in the running directory.
 */
object VersionManager {

    /**
     * The file that contains the information about the Dragonfly version in JSON format.
     */
    private val file = File("dragonfly-version.json")

    /**
     * The json object parsed from the content of the [file].
     */
    private var jsonObject = readFile()

    /**
     * Json content for the stable channel.
     */
    val stable by jsonObject

    /**
     * Json content for the early access channel.
     */
    val earlyAccess by jsonObject

    /**
     * Reloads the [jsonObject] from the [file].
     */
    fun reloadJsonObject() {
        jsonObject = readFile()
    }

    /**
     * Reads the [file] and parses its content to a json object.
     */
    private fun readFile(): JsonObject = file.reader().use { JsonParser().parse(it) }.asJsonObject
}

/**
 * A simple property delegation function to retrieve values of the json object.
 */
private operator fun JsonObject.getValue(thisRef: Any?, property: KProperty<*>): JsonElement {
    return get(property.name).asJsonObject
}