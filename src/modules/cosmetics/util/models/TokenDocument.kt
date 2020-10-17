package modules.cosmetics.util.models

import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("cosmetic_id")
    val cosmeticId: Int,

    /**
     * When the token was created (UNIX timestamp)
     */
    @JsonProperty("created_date")
    val createdDate: Long,

    /**
     * The Dragonfly account that created this token
     */
    @JsonProperty("created_account")
    val createdAccount: String,

    /**
     * Whether the token has been redeemed
     */
    @JsonProperty("redeemed")
    var isRedeemed: Boolean = false,

    /**
     * The Dragonfly account that redeemed this token
     */
    @JsonProperty("redeemed_account")
    var redeemedAccount: String? = null,

    /**
     * The time when the token was redeemed (UNIX timestamp)
     */
    @JsonProperty("redeemed_date")
    var redeemedDate: Long? = null,

    /**
     * The MongoDB object id to identify the document in the database
     */
    val _id: ObjectId? = null
) {

    companion object {

        /**
         * Available chars for generating cosmetic tokens.
         */
        private const val availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789012345678901234567890"

        /**
         * Generates the payload string for a cosmetic token.
         */
        fun generatePayload(): String = buildString {
            append("CSM-")
            append((1..6).map { availableChars.random() }.joinToString(""))
            append("-")
            append((1..6).map { availableChars.random() }.joinToString(""))
        }
    }
}