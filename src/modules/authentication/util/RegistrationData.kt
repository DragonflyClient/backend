package modules.authentication.util

data class RegistrationData(
    val email: String,
    val code: String,
    val username: String,
    val password: String
)