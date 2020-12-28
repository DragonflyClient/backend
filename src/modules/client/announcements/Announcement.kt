package modules.client.announcements

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

data class Announcement(
    /** Unique ObjectId to identify the announcement */
    @JsonProperty("_id")
    @Transient
    var identifier: ObjectId? = null,

    /** Header (title) for the announcement */
    val title: String,

    /** Text content of the announcement */
    val content: String,

    /** Link to the image of the announcement */
    val image: String,

    /** Dragonfly UUID of the account that published the announcement */
    @JsonProperty("published_by")
    var publishedBy: String? = null,
) {
    /** Unix timestamp of the publication date parsed from the [identifier] */
    @JsonIgnore
    val publishedOn: Int? = identifier?.timestamp
}