package modules.cosmetics

import core.Module
import kotlinx.coroutines.runBlocking
import modules.cosmetics.routes.*
import modules.cosmetics.util.*
import org.bson.Document
import java.util.*

object CosmeticsModule : Module(
    "Cosmetics",
    FindRoute,
    AvailableRoute,
    ToggleRoute,
    BindRoute,
    ConfigureRoute
)

fun main() = runBlocking {
    CosmeticsController.insert(
        Filter.new().minecraft("bc73becb-bc3b-487b-95a2-35ab00e11d0a"),
        CosmeticItem(
            cosmeticId = 1,
            enabled = true,
            config = Document().append("color", "hello"),
            minecraft = "bc73becb-bc3b-487b-95a2-35ab00e11d0a",
            cosmeticQualifier = UUID.randomUUID().toString()
        )
    )
}