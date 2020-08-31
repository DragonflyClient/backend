package modules.cosmetics.util.config

import com.google.gson.JsonElement

/**
 * Enum with all available property types.
 *
 * @param converter the function that tries to convert a json element to
 * the specific type and returns null or throws an exception if this fails
 */
enum class EnumPropertyType(
    private val converter: (JsonElement) -> Any?
) {
    BOOLEAN({ elem -> elem.asBoolean }),
    DOUBLE({ elem -> elem.asDouble }),
    COLOR({ elem ->
        elem.asJsonObject.also {
            it["red"].asInt
            it["green"].asInt
            it["blue"].asInt
            it["alpha"].asInt
            it["rainbow"].asBoolean
        }
    });

    /**
     * Runs the [converter] returning null if any exception occurred.
     */
    fun convert(elem: JsonElement): Any? = elem.runCatching(converter).getOrNull()
}