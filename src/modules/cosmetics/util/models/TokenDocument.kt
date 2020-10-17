package modules.cosmetics.util.models


import com.google.gson.annotations.SerializedName
import org.bson.types.ObjectId

/**
 * Represents a cosmetics token in the Dragonfly database.
 *
 * - Database: `cosmeticsDB`
 * - Collection: `tokens`
 */
data class TokenDocument(

    /**
     * The payload string that uniquely identifies the token. Always starts with 'CSM'.
     */
    val payload: String,

    /**
     * The id of the cosmetic that is unlocked with the token
     */
    @SerializedName("cosmetic_id") val cosmeticId: Int,

    /**
     * When the token was created (UNIX timestamp)
     */
    @SerializedName("created_date") val createdDate: Long,

    /**
     * The Dragonfly account that created this token
     */
    @SerializedName("created_account") val createdAccount: String,

    /**
     * Whether the token has been redeemed
     */
    var redeemed: Boolean = false,

    /**
     * The Dragonfly account that redeemed this token
     */
    @SerializedName("redeemed_account") var redeemedAccount: String? = null,

    /**
     * The time when the token was redeemed (UNIX timestamp)
     */
    @SerializedName("redeemed_date") var redeemedDate: Long? = null,

    /**
     * The MongoDB object id to identify the document in the database
     */
    val _id: ObjectId? = null
)