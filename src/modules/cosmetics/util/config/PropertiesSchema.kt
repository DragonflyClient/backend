package modules.cosmetics.util.config

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Represents a schema of properties and its types for a specific available
 * cosmetic against which all incoming configurations for the cosmetic item
 * are checked.
 */
class PropertiesSchema private constructor(
    private val properties: Map<String, EnumPropertyType>
) {
    companion object {

        /**
         * Creates a new properties schema by parsing the [obj] to the [properties] map
         * in the format `name: TYPE` where the type is an enum value of [EnumPropertyType].
         */
        fun create(obj: JsonObject): PropertiesSchema {
            val gson = Gson()
            val map = mutableMapOf<String, EnumPropertyType>()
            for ((property, typeName) in obj.entrySet()) {
                map[property] = gson.fromJson(typeName, EnumPropertyType::class.java)
            }

            return PropertiesSchema(map)
        }
    }

    /**
     * Cleans the incoming [obj] removing all properties that are not contained in
     * the [properties] map or that have an invalid type.
     */
    fun clean(obj: JsonObject): JsonObject {
        val gson = Gson()
        val result = JsonObject()

        for ((key, value) in obj.entrySet()) {
            if (properties.containsKey(key)) {
                val cleanedValue = properties[key]!!.convert(value) ?: continue
                result.add(key, gson.toJsonTree(cleanedValue))
            }
        }

        return result
    }
}