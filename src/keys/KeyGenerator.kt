package keys

/**
 * A simple utility object to generate keys in the required format.
 */
object KeyGenerator {

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
    fun generateSafeKey(): String {
        var key = generateKey()

        while (DragonflyBackend.firestore.collection("keys").document(key).get().get().exists()) {
            key = generateKey()
        }

        return key
    }
}