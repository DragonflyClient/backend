package modules.cosmetics.util.models

import org.bson.Document

data class CosmeticItem(
    var cosmeticId: Int,
    val cosmeticQualifier: String,
    var enabled: Boolean,
    var minecraft: String? = null,
    var config: Document
)