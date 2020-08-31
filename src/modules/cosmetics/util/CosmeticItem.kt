package modules.cosmetics.util

import org.bson.Document
import java.util.*

data class CosmeticItem(
    var cosmeticId: Int,
    val uniqueIdentifier: String = UUID.randomUUID().toString(),
    var enabled: Boolean,
    var minecraft: String? = null,
    var config: Document
)