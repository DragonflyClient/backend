package modules.minecraft.util

import DragonflyBackend
import com.auth0.jwt.JWT
import modules.authentication.util.Account
import modules.authentication.util.AuthenticationManager
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.util.*

object MinecraftLinkManager {

    /** The Dragonfly database */
    private val database = DragonflyBackend.mongo.getDatabase("dragonfly")

    /** The collection in which the [accounts][Account] are stored */
    private val accountsCollection = database.getCollection<Account>("accounts")

    /** The collection that manages the uuids */
    private val linksCollection = database.getCollection<MinecraftLink>("minecraft-links")

    /**
     * Verifies that the given [minecraftToken] is valid and returns the UUID associated with the token.
     * Returns null if the token is invalid.
     */
    fun verifyAccount(minecraftToken: String): UUID? {
        val valid = khttp.post(
            url = "https://authserver.mojang.com/validate",
            json = mapOf(
                "accessToken" to minecraftToken
            )
        ).statusCode == 204

        return valid.takeIf { it }?.runCatching {
            parseWithoutDashes(JWT.decode(minecraftToken).getClaim("spr").asString())
        }?.getOrNull()
    }

    /**
     * Tries to find a Dragonfly account by the Minecraft [uuid].
     */
    suspend fun getByMinecraftUUID(uuid: UUID): Account? = getByMinecraftUUID(uuid.toString())

    /**
     * Tries to find a Dragonfly account by the Minecraft [uuid].
     */
    suspend fun getByMinecraftUUID(uuid: String): Account? {
        val dragonflyUUID = linksCollection.findOne(MinecraftLink::minecraft eq uuid)
        return dragonflyUUID?.let { AuthenticationManager.getByUUID(it.dragonfly) }
    }

    /**
     * Links the given [minecraft] UUID to the Dragonfly [account].
     */
    suspend fun link(account: Account, minecraft: UUID) {
        val linked = account.linkedMinecraftAccounts?.toMutableList() ?: mutableListOf()
        linked.add(minecraft.toString())
        account.linkedMinecraftAccounts = linked
        accountsCollection.updateOne(Account::uuid eq account.uuid, setValue(Account::linkedMinecraftAccounts, linked))

        val minecraftLink = MinecraftLink(minecraft.toString(), account.uuid)
        linksCollection.insertOne(minecraftLink)
    }

    /**
     * Removes the link from the Dragonfly [account] to the [minecraft] UUID.
     */
    suspend fun unlink(account: Account, minecraft: UUID) {
        CosmeticsController.updateEach(Filter.new().minecraft(minecraft.toString())) { it.minecraft = null }

        val linked = account.linkedMinecraftAccounts?.toMutableList()?.also { it.remove(minecraft.toString()) } ?: mutableListOf()
        account.linkedMinecraftAccounts = linked
        accountsCollection.updateOne(Account::uuid eq account.uuid, setValue(Account::linkedMinecraftAccounts, linked))

        linksCollection.deleteOne(MinecraftLink::minecraft eq minecraft.toString(), MinecraftLink::dragonfly eq account.uuid)
    }

    fun parseWithoutDashes(digits: String): UUID = UUID.fromString(
        if (digits.contains("-")) digits else digits.replace("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5")
    )
}