package modules.authentication.util

/**
 * The default permission levels that are provided for Dragonfly accounts.
 *
 * @param value an integer-representation of the level to store and compare them
 */
enum class PermissionLevel(val value: Int) {

    USER(0),
    PARTNER(3),
    MODERATOR(7),
    CONTRIBUTOR(8),
    MANAGER(9),
    OPERATOR(10);

    companion object {
        /**
         * Retrieves the highest [PermissionLevel] by the integer representation that is below
         * or at the same level of the integer [value].
         */
        fun byValue(value: Int): PermissionLevel? = values().lastOrNull { it.value <= value }

        /**
         * Retrieves the [PermissionLevel] that has the exact same [value].
         */
        fun byValueExact(value: Int): PermissionLevel? = values().lastOrNull { it.value == value }
    }
}