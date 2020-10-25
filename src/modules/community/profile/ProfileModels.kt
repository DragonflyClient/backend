package modules.community.profile

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

/**
 * The model class for all documents in the profiles collection.
 */
data class Profile(

    /**
     * The uuid of the Dragonfly account that this profile belongs to.
     */
    @JsonProperty("dragonfly_uuid")
    val dragonflyUUID: String,

    /**
     * The list of the notifications that this profile has received. The notifications are
     * represented by their object ids and have to be collected from the notifications collection.
     */
    val notifications: MutableList<String> = mutableListOf(),

    /**
     * Unique object id to identify this profile.
     */
    val _id: ObjectId? = null
)