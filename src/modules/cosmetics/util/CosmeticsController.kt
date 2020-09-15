package modules.cosmetics.util

import DragonflyBackend
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import core.fatal
import modules.cosmetics.util.config.PropertiesSchema
import modules.minecraft.util.MinecraftLinkManager
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

object CosmeticsController {

    private val database = DragonflyBackend.mongo.getDatabase("dragonfly")

    private val collection = database.getCollection<CosmeticsDocument>("cosmetics")

    private val available = database.getCollection<Document>("available-cosmetics")

    /**
     * Returns all items in the 'available-cosmetics' collection.
     */
    suspend fun getAvailable(): List<Document> = available.find().toList()
        .map { it.apply { remove("_id") } }

    /**
     * Returns an available cosmetic item by its [cosmeticId].
     */
    suspend fun getAvailableById(cosmeticId: Int) = available.findOne(Filters.eq("cosmeticId", cosmeticId))

    /**
     * Generates a [PropertiesSchema] based on the available cosmetic specified by the [cosmeticId].
     */
    suspend fun getPropertiesSchema(cosmeticId: Int): PropertiesSchema {
        val availableCosmetic = getAvailableById(cosmeticId) ?: fatal("Invalid cosmetic id")

        val properties = availableCosmetic["properties"] as Document
        val jsonObject = JsonParser.parseString(properties.toJson()).asJsonObject
        return PropertiesSchema.create(jsonObject)
    }

    /**
     * Returns all cosmetics on the Dragonfly account specified by the [filter] and bound to the
     * Minecraft account specified by the [filter]. If no Minecraft account is specified, this will
     * return all cosmetics on the Dragonfly account.
     */
    suspend fun find(filter: Filter): List<CosmeticItem> {
        val cosmetics = collection.findOne(filter.toBson())?.cosmetics
        return cosmetics?.filter { filter.checkBound(it) } ?: listOf()
    }

    /**
     * Overrides the cosmetic data on the Dragonfly account specified by the [filter]. Note that
     * this function will override all cosmetics and not only the ones that are bound to the Minecraft
     * account specified by the [filter]. This means that the [Filter.minecraft] account is only used
     * to query for the Dragonfly account that is linked to the Minecraft account.
     */
    suspend fun override(filter: Filter, new: List<CosmeticItem>) {
        val result = collection.updateOne(filter.toBson(), setValue(CosmeticsDocument::cosmetics, new))

        if (result.matchedCount == 0L) {
            val document = CosmeticsDocument(filter.getDragonflyUUID(), new)
            collection.insertOne(document)
        }
    }

    /**
     * Updates the [cosmetics collection][CosmeticsDocument.cosmetics] of the Dragonfly account specified
     * by the [filter]. This function calls the update [block] on a mutable copy of the collection and
     * then overrides the existing one with the new updated one. This means that the whole collection is
     * overwritten regardless of whether the [CosmeticItem] is bound to the Minecraft account specified
     * by the [filter].
     */
    suspend fun update(filter: Filter, block: suspend (MutableList<CosmeticItem>) -> Unit) {
        val existing = find(Filter.new().dragonfly(filter.getDragonflyUUID()))
        val new = existing.toMutableList()
        block(new)

        override(filter, new)
    }

    /**
     * Updates each [CosmeticItem] in the cosmetics collection of the Dragonfly account specified by
     * the [filter] by calling the update [block] on every entry in the collection. Note that (unlike
     * [update]) this function filters out any cosmetics that are not bound to the Minecraft account
     * specified by the filter. These cosmetics remain unchanged after the update is executed and the
     * old collection is [overwritten][override] by the updated one.
     */
    suspend fun updateEach(filter: Filter, block: suspend (CosmeticItem) -> Unit) =
        update(filter) {
            it.filter { item -> filter.checkBound(item) }
                .forEach { item -> block(item) }
        }

    /**
     * Calls the [updateEach] function while additionally checking if the [cosmeticQualifier] matches.
     */
    suspend fun updateEach(filter: Filter, cosmeticQualifier: String, block: suspend (CosmeticItem) -> Unit): Boolean {
        var found = false
        updateEach(filter) {
            if (it.cosmeticQualifier == cosmeticQualifier) {
                block(it)
                found = true
            }
        }
        return found
    }

    /**
     * Inserts the new [cosmeticItem] into the cosmetics collection of the Dragonfly account specified
     * by the [filter].
     */
    suspend fun insert(filter: Filter, cosmeticItem: CosmeticItem) = update(filter) { it.add(cosmeticItem) }
}

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
                fatal("The given Minecraft UUID is not linked to a Dragonfly account!")
            }
        }

        error("At least one of both 'minecraft' or 'dragonfly' specifier must be given!")
    }
}