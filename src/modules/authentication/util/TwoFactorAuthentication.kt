package modules.authentication.util

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil
import core.checkedError
import log
import modules.authentication.util.models.Account
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * The core class of two factor authentication that manages the enabling/disabling
 * and verification of TOTP.
 */
object TwoFactorAuthentication {

    /**
     * Available chars for generating backup codes.
     */
    private const val availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789012345678901234567890"

    /**
     * Enables two factor authentication for the given [account].
     */
    suspend fun request2FA(account: Account) = with(account) {
        prohibit2FA()

        val newSecret = TimeBasedOneTimePasswordUtil.generateBase32Secret()
        with(twoFactorAuthentication) {
            requested = true
            secret = newSecret
        }
        save()
    }

    /**
     * Enables two factor authentication for the given [account].
     */
    suspend fun enable2FA(account: Account, code: String) = with(account.twoFactorAuthentication) {
        if (!requested) checkedError("You have to request 2FA first before trying to enable it!")
        account.prohibit2FA()

        val current = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(account.twoFactorAuthentication.secret!!)
        if (code.replace(" ", "") != current) checkedError("Invalid 2FA code")

        enabled = true
        backupCodes = generateBackupCodes().toMutableList()
        account.save()
    }

    /**
     * Disables two factor authentication for the given [account].
     */
    suspend fun disable2FA(account: Account) = with(account) {
        require2FA()

        with(twoFactorAuthentication) {
            enabled = false
            requested = false
            secret = null
            backupCodes = null
        }
        account.save()
    }

    /**
     * Returns whether the given [code] matches the current TOTP of the given [account].
     */
    suspend fun verifyCode(account: Account, code: String): Boolean {
        account.require2FA()

        if (code.contains("-") && account.twoFactorAuthentication.backupCodes!!.contains(code)) {
            log("${account.username} is using his backup code $code")
            account.twoFactorAuthentication.backupCodes!!.remove(code)
            account.save()
            return true
        }

        val current = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(account.twoFactorAuthentication.secret!!)
        return code.replace(" ", "") == current
    }

    /**
     * Generates the QR code that adds the 2FA secret of the [account] to the authenticator app.
     * This function returns an url that contains an image of the QR code generated with the
     * Google APIs.
     */
    fun generateQRCode(account: Account): String {
        val name = account.email ?: account.username
        val urlEncodedName = URLEncoder.encode("Dragonfly ($name)", StandardCharsets.UTF_8.toString())

        return "https://chart.googleapis.com/chart" +
                "?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr" +
                "&chl=otpauth://totp/$urlEncodedName?secret=${account.twoFactorAuthentication.secret!!}"
    }

    /**
     * Generates 20 backup codes by using a combination of two [random char sequences][getRandomChars].
     */
    private fun generateBackupCodes(): List<String> = (1..20).map {
        "${getRandomChars()}-${getRandomChars()}"
    }

    /**
     * Takes 6 random chars from the [availableChars] string.
     */
    private fun getRandomChars() = (1..6).map { availableChars.random() }.joinToString("")

    /**
     * Throws an exception if two factor authentication is **not** set up.
     */
    private fun Account.require2FA() {
        if (!twoFactorAuthentication.enabled) checkedError("Two factor authentication is not enabled on this account")
    }

    /**
     * Throws an exception if two factor authentication **is** set up.
     */
    private fun Account.prohibit2FA() {
        if (twoFactorAuthentication.enabled) checkedError("Two factor authentication is already enabled on this account")
    }
}