package modules.keys

import DragonflyBackend
import log
import org.litote.kmongo.eq

/**
 * A simple utility object to generate keys in the required format.
 */
object KeyGenerator {

    /**
     * The mongodb collection in which the keys are stored.
     */
    val collection = DragonflyBackend.mongo.getDatabase("dragonfly").getCollection<KeyDocument>("keys")

    /**
     * All characters that are available for generating the key.
     */
    private val availableCharacters = (48..57) + (65..90)

    /**
     * Utility function to generate a sequence of random characters selected
     * from [availableCharacters] with the [length].
     */
    private fun random(length: Int): String = (1..length).joinToString(separator = "") { availableCharacters.random().toChar().toString() }

    /**
     * Generates a key that consists of 28 randomly selected characters split according
     * to the following format: `6-8-8-6`.
     */
    private fun generateKey(): String = "${random(6)}-${random(8)}-${random(8)}-${random(6)}"

    /**
     * Uses a safe method to generate the key by searching and avoiding duplicates
     * which can potentially break the key system.
     */
    suspend fun generateSafeKey(): String {
        var key = generateKey()

        while (collection.findOne(KeyDocument::key eq key) != null) {
            log("Key $key is already used. Generating new one...")
            key = generateKey()
        }

        return key
    }
}
