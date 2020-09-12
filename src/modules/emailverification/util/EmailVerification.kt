package modules.emailverification.util

import DragonflyBackend
import kotlinx.coroutines.runBlocking
import modules.authentication.util.Account
import modules.authentication.util.AuthenticationManager
import org.apache.commons.codec.digest.DigestUtils
import org.litote.kmongo.eq

object EmailVerification {

    private val collection = DragonflyBackend.database.getCollection<EVDocument>("email-verification")

    /**
     * Generates the random code for the email verification.
     */
    private fun generateCodeRandom(): String {
        val available = "abcdefghijklmnopqrstuvwxyzABCEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        return DigestUtils.sha1Hex(available.takeRandom(100))
    }

    suspend fun findVerification(emailIn: String): EVDocument? {
        val email = emailIn.toLowerCase()

        val verification = collection.findOne(EVDocument::email eq email)
        return verification?.withExpiration()
    }

    suspend fun startVerification(emailIn: String): EVDocument {
        val email = emailIn.toLowerCase()

        val existingAccount = AuthenticationManager.accountsCollection.findOne(Account::email eq email)
        if (existingAccount != null) error("This email address is already in use!")

        val existingVerification = findVerification(email)
        if (existingVerification != null) error("This email address is already part of a running verification process!")

        val currentTimeMillis = System.currentTimeMillis()
        val expiresAt = currentTimeMillis + (1000L * 60 * 10)
        val document = EVDocument(email, generateCodeRandom(), expiresAt)
        collection.insertOne(document)
        return document
    }

    private suspend fun EVDocument.withExpiration(): EVDocument? {
        if (expiresAt > System.currentTimeMillis()) return this

        collection.deleteOne(::code eq code)
        return null
    }
}

fun main(): Unit = runBlocking {
//    EmailVerification.startVerification("theincxption@gmail.com")
    println(EmailVerification.findVerification("theincxption@gmail.com"))
}

fun String.takeRandom(amount: Int) = (0..amount).map { random() }.joinToString("")