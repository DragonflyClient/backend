package modules.diagnostics.util

data class CrashReport(
    val cause: String,
    val comment: String,
    val user: String,
    val dragonflyUser: String?,
    val full: String
)