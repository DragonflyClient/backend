package modules.store

import core.Module
import modules.store.routes.ExecutePaymentRoute

object StoreModule : Module(
    "Store",
    ExecutePaymentRoute
)