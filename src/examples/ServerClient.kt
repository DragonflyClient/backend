package examples

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.network.tls.tls
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object Server {
    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind("127.0.0.1", 34456)

            while (true) {
                val socket = server.accept()

                launch {
                    println("Socket accepted: ${socket.remoteAddress}")

                    val input = socket.openReadChannel()
                    val output = socket.openWriteChannel(autoFlush = true)

                    try {
                        while (true) {
                            val line = input.readUTF8Line()

                            println("${socket.remoteAddress}: $line")
                            output.writeStringUtf8("You said: $line\r\n")
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        socket.close()
                    }
                }
            }
        }
    }
}

object Client {
    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect("127.0.0.1", 34456).tls(coroutineContext)
            val w = socket.openWriteChannel()
            w.writeStringUtf8("Hello World\n")
            w.flush()
            val r = socket.openReadChannel()
            println(r.readUTF8Line())
        }
    }
}