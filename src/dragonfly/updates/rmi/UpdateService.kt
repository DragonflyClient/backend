package dragonfly.updates.rmi

import java.rmi.Remote
import java.rmi.RemoteException

interface UpdateService : Remote {

    @Throws(RemoteException::class)
    fun publishUpdate(
        version: String,
        title: String?,
        releaseNotes: String?,
        releaseDate: Long,
        earlyAccess: Boolean,
        stable: Boolean,
        username: String,
        password: String
    )
}
