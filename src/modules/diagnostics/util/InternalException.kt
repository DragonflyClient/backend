package modules.diagnostics.util

import kotlin.math.absoluteValue

class InternalException(
    val category: String,
    val title: String?,
    val exception: String,
    val data: Any?
) {
    val name: String
        get() = title ?: "Exception #${exception.hashCode().absoluteValue}"
}