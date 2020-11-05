package modules.partner.partners

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

/**
 * This class represents a partner document in the database. Every partner
 * has an associated Dragonfly account which is referred by the [dragonflyUUID].
 */
data class Partner(

    /**
     * The UUID of the Dragonfly account that this partner owns.
     */
    @JsonProperty("dragonfly_uuid")
    val dragonflyUUID: String,

    /**
     * The object id that uniquely identifies this partner.
     */
    @JsonProperty("_id")
    val partnerId: ObjectId = ObjectId.get()
)