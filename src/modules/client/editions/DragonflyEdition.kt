package modules.client.editions

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

data class DragonflyEdition(
    @JsonProperty("_id")
    val identifier: ObjectId,
    val title: String,
    val version: String,
    @JsonProperty("minecraft_version")
    val minecraftVersion: String,
    val description: String,
    val tags: List<String>,
    @JsonProperty("injection_hook")
    val injectionHook: String,
)