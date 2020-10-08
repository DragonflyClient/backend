package modules.authentication.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import modules.authentication.util.models.Account
import secrets.SECRET
import java.util.*

object JwtConfig {

    private const val secret = SECRET
    private const val issuer = "Inception Cloud Dragonfly"
    private const val validityInMs = 1000 * 60 * 60 * 24 * 30L // 30 days
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of User and Account
     */
    fun makeToken(account: Account): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("uuid", account.uuid)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(
        System.currentTimeMillis() + validityInMs
    )
}
