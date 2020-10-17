package modules.cosmetics.util

import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import core.MongoDB
import core.checkedError
import modules.cosmetics.util.config.PropertiesSchema
import modules.cosmetics.util.models.*
import org.bson.Document
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

object CosmeticsController {

    private val collection = MongoDB.dragonflyDB.getCollection<CosmeticsDocument>("cosmetics")

    private val available = MongoDB.dragonflyDB.getCollection<Document>("available-cosmetics")

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
    suspend fun getPropertiesSchema(cosmeticId: Int): PropertiesSchema? {
        val availableCosmetic = getAvailableById(cosmeticId) ?: checkedError("Invalid cosmetic id")

        val properties = availableCosmetic["properties"] as? Document?
        val jsonObject = properties?.toJson()?.let { JsonParser.parseString(it) }?.asJsonObject
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

    /**
     * The collection that contains all cosmetic tokens.
     */
    private val tokens = MongoDB.cosmeticsDB.getCollection<TokenDocument>("tokens")

    /**
     * Returns a token by its payload string.
     */
    suspend fun getToken(payload: String): TokenDocument? = tokens.findOne(TokenDocument::payload eq payload)
}

