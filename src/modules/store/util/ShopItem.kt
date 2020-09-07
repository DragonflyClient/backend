package modules.store.util

import com.google.gson.JsonObject

data class ShopItem(
    val name: String,
    val id: String,
    val originalPrice: Int,
    val price: Int,
    val currency: String,
    val media: JsonObject,
    val description: String,
    val cosmeticId: Int?
)