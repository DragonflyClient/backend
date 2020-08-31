package modules.cosmetics

import core.Module
import kotlinx.coroutines.runBlocking
import modules.cosmetics.routes.AvailableRoute
import modules.cosmetics.routes.FindRoute
import modules.cosmetics.util.*
import org.bson.Document

object CosmeticsModule : Module(
    "Cosmetics",
    FindRoute,
    AvailableRoute
)

fun main() = runBlocking {
    CosmeticsController.insert(
        Filter.new().minecraft("bc73becb-bc3b-487b-95a2-35ab00e11d0a"),
        CosmeticItem(cosmeticId = 1, enabled = true, config = Document().append("color", "hello"), minecraft = "bc73becb-bc3b-487b-95a2-35ab00e11d0a")
    )
}