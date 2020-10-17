package modules.cosmetics.util

import core.checkedError
import io.ktor.http.*
import modules.minecraft.util.MinecraftLinkManager
import org.bson.conversions.Bson
import org.litote.kmongo.eq

/**
 * Convenience class for expressing a filter to collect cosmetics.
 *
 * This filter can specify the [minecraft] and/or [dragonfly] account to query the
 * cosmetics database. Please note that at least one of both must be given since
 * otherwise [getDragonflyUUID] will throw an exception.
 */
class Filter private constructor() {

    companion object {

        /**
         * Create a new filter.
         */
        fun new() = Filter()
    }

    /**
     * Matches the Dragonfly UUID if given.
     */
    private var dragonfly: String? = null

    /**
     * Matches the Minecraft UUID if given.
     */
    var minecraft: String? = null

    /**
     * Cache for the result of [getDragonflyUUID].
     */
    private var computedDragonflyUUID: String? = null

    /**
     * Sets the [dragonfly] UUID to be matched.
     */
    fun dragonfly(uuid: String) = apply { dragonfly = uuid }

    /**
     * Sets the [minecraft] UUID to be matched.
     */
    fun minecraft(uuid: String) = apply { minecraft = uuid }

    /**
     * Check if the given [cosmeticItem] is able to pass the filter based on whether it is bound
     * to the [minecraft] account. This function returns false if the [cosmeticItem] is not bound
     * to a Minecraft account or if [minecraft] is not set.
     */
    fun checkBound(cosmeticItem: CosmeticItem): Boolean {
        if (minecraft == null) return true
        if (cosmeticItem.minecraft == null) return false
        return minecraft == cosmeticItem.minecraft
    }

    /**
     * Creates a bson filter based on the [Dragonfly UUID][getDragonflyUUID] that this filter
     * specifies.
     */
    suspend fun toBson(): Bson {
        return CosmeticsDocument::dragonflyUUID eq getDragonflyUUID()
    }

    /**
     * Returns a Dragonfly UUID specified by this filter. Uses either the [dragonfly] UUID or the
     * UUID of the Dragonfly account that the [minecraft] account is linked with while the priority
     * is on the former.
     */
    suspend fun getDragonflyUUID(): String {
        if (computedDragonflyUUID != null) return computedDragonflyUUID!!

        if (dragonfly != null) {
            return dragonfly!!.also { computedDragonflyUUID = it }
        } else if (minecraft != null) {
            val account = MinecraftLinkManager.getByMinecraftUUID(minecraft!!)
            if (account != null) {
                return account.uuid.also { computedDragonflyUUID = it }
            } else {
                checkedError("The given Minecraft UUID is not linked to a Dragonfly account!", HttpStatusCode.NotImplemented)
            }
        }

        error("At least one of both 'minecraft' or 'dragonfly' specifier must be given!")
    }
}