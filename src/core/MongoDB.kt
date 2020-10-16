package core

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import secrets.CONNECTION_STRING

/**
 * The core bridge between the backend and the database.
 */
object MongoDB {

    /**
     * A coroutine-based KMongo client to connect to the database
     */
    val client = KMongo.createClient(CONNECTION_STRING).coroutine

    /**
     * The legacy general-purpose Dragonfly database that should be split up into more databases in the near future.
     */
    val dragonflyDB = client.getDatabase("dragonfly")

    /**
     * The database that contains all information about cosmetics for the Dragonfly client.
     */
    val cosmeticsDB = client.getDatabase("cosmeticsDB")
}