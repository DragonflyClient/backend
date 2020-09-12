package modules.emailverification.util

data class EVDocument(
    val email: String,
    val code: String,
    val expiresAt: Long,
    val status: Status = Status.PENDING
)

enum class Status {
    PENDING,
    CONFIRMED
}