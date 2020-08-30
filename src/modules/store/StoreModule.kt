package modules.store

import core.Module
import modules.store.routes.stripe.PaymentIntentSucceededRoute

object StoreModule : Module(
    "Store",
    PaymentIntentSucceededRoute
)