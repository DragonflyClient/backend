package version.update

import com.google.gson.*
import log
import version.Version
import java.io.File

/**
 * Reads the version from the dragonfly-update-history.json file in the running directory.
 */
object UpdateHistory {

    /**
     * The file that contains the information about the recent Dragonfly updates in the
     * json format.
     */
    private val file = File("dragonfly-update-history.json")

    /**
     * The json object parsed from the content of the [file].
     */
    private var jsonObject = readFile()

    /**
     * Json content for the stable channel.
     */
    private val stable: JsonArray
        get() = jsonObject["stable"].asJsonArray

    /**
     * Json content for the early access channel.
     */
    private val earlyAccess: JsonArray
        get() = jsonObject["earlyAccess"].asJsonArray

    /**
     * The version of the Dragonfly Installer
     */
    val installer: String
        get() = jsonObject["installer"].asString

    /**
     * Returns the update history since the given [version].
     */
    fun getUpdateHistorySince(channel: UpdateChannel, version: Version): List<Update> =
        getUpdateHistory(channel).filter {
            compareVersions(
                Version.of(it.version) ?: Version(100, 0, 0, 0),
                version
            ) == 1
        }

    /**
     * Returns the update history for the given [channel] from the json file.
     */
    fun getUpdateHistory(channel: UpdateChannel): List<Update> {
        val array = if (channel == UpdateChannel.STABLE) stable else earlyAccess
        val gson = Gson()
        return array.map { gson.fromJson(it.asJsonObject, Update::class.java) }
    }

    /**
     * Publishes an update by appending it to the top of the specified channel stack.
     */
    fun publishUpdate(channel: UpdateChannel, update: Update) {
        val array = if (channel == UpdateChannel.STABLE) stable else earlyAccess
        val newArray = JsonArray().apply {
            add(Gson().toJsonTree(update))
            addAll(array)
        }

        val key = if (channel == UpdateChannel.STABLE) "stable" else "earlyAccess"

        jsonObject.remove(key)
        jsonObject.add(key, newArray)
        file.writeText(
            Gson().newBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(jsonObject)
        )


        reloadJsonObject()
    }

    /**
     * Reloads the [jsonObject] from the [file].
     */
    fun reloadJsonObject() {
        jsonObject = readFile()
        log("The update history has been reloaded!")
    }

    /**
     * Reads the [file] and parses its content to a json object.
     */
    private fun readFile(): JsonObject = file.reader().use { JsonParser().parse(it) }.asJsonObject

    /**
     * Compares the local to the remote version dropping the patch part of the version.
     */
    private fun compareVersions(first: Version, second: Version): Int =
        compareVersionParts(first.toVersionParts(), second.toVersionParts())

    /**
     * Compares the given parts of the [first] and the [second] version and returns
     *
     * - 1 if the [first] version is newer
     * - 0 if the versions are identical
     * - -1 if the [second] version is newer.
     */
    private fun compareVersionParts(first: List<Int>, second: List<Int>): Int {
        for (index in first.indices) {
            val l = first[index]
            val r = second[index]

            if (l < r) {
                return -1
            } else if (l > r) {
                return 1
            }
        }

        return 0
    }
}