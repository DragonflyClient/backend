package modules.store.util

data class Payment(
    val provider: String,
    val paymentId: String,
    val payerEmail: String,
    val paymentState: String,
    val receivedAmount: Int,
    val receivedCurrency: String,
    val creationDate: Long,
    val dragonflyToken: String,
    val itemId: String,
    val itemName: String,
    val itemPrice: Int,
    val itemCurrency: String,
    var executed: Boolean? = false
)