package modules.minecraft.util

import DragonflyBackend
import com.auth0.jwt.JWT
import modules.authentication.util.Account
import modules.authentication.util.AuthenticationManager
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.util.*

object MinecraftLinkManager {

    /** The Dragonfly database */
    private val database = DragonflyBackend.mongo.getDatabase("dragonfly")

    /** The collection in which the [accounts][Account] are stored */
    private val accountsCollection = database.getCollection<Account>("accounts")

    /** The collection that manages the uuids */
    private val linksCollection = database.getCollection<MinecraftLink>("mojang-links")

    fun verifyAccount(mojangToken: String): UUID? {
        val valid = khttp.post(
            url = "https://authserver.mojang.com/validate",
            json = mapOf(
                "accessToken" to mojangToken
            )
        ).statusCode == 204

        return valid.takeIf { it }?.runCatching {
            parseWithoutDashes(JWT.decode(mojangToken).getClaim("spr").asString())
        }?.getOrNull()
    }

    suspend fun getByMojangUUID(uuid: UUID): Account? {
        val dragonflyUUID = linksCollection.findOne(MinecraftLink::minecraft eq uuid.toString())
        return dragonflyUUID?.let { AuthenticationManager.getByUUID(it.dragonfly) }
    }

    suspend fun link(account: Account, mojang: UUID) {
        val linked = account.linkedMojangAccounts?.toMutableList() ?: mutableListOf()
        linked.add(mojang.toString())
        account.linkedMojangAccounts = linked
        accountsCollection.updateOne(Account::uuid eq account.uuid, setValue(Account::linkedMojangAccounts, linked))

        val mojangLink = MinecraftLink(mojang.toString(), account.uuid)
        linksCollection.insertOne(mojangLink)
    }

    suspend fun unlink(account: Account, mojang: UUID) {
        val linked = account.linkedMojangAccounts?.toMutableList()?.also { it.remove(mojang.toString()) } ?: mutableListOf()
        account.linkedMojangAccounts = linked
        accountsCollection.updateOne(Account::uuid eq account.uuid, setValue(Account::linkedMojangAccounts, linked))

        linksCollection.deleteOne(MinecraftLink::minecraft eq mojang.toString())
    }

    private fun parseWithoutDashes(digits: String): UUID = UUID.fromString(
        digits.replace("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5")
    )
}