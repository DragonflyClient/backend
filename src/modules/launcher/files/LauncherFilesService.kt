package modules.launcher.files

import log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.xml.bind.annotation.adapters.HexBinaryAdapter

class LauncherFilesService {
    var fileIndex: List<String> = emptyList()
    private val rootDirectory = File("/var/www/cdn/dragonfly/client/")

    init {
        loadFiles()
    }

    fun loadFiles() {
        val files = getFilesInDirectory(rootDirectory)
            .filter { !it.name.endsWith(".sha1") }
        files.forEach { generateSHA1(it) }
        fileIndex = files.map { it.toRelativeString(rootDirectory) }
        log("Loaded ${files.size} files for the launcher")
    }

    private fun generateSHA1(file: File) {
        val parent = file.parentFile
        val hashFile = File(parent, file.name + ".sha1")
        hashFile.writeText(calcSHA1(file) + " " + file.name)
    }

    private fun getFilesInDirectory(dir: File): List<File> {
        return (dir.listFiles() ?: return emptyList())
            .flatMap { if (it.isDirectory) getFilesInDirectory(it) else listOf(it) }
    }

    private fun calcSHA1(file: File): String {
        val sha1 = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var len = input.read(buffer)
            while (len != -1) {
                sha1.update(buffer, 0, len)
                len = input.read(buffer)
            }
            return HexBinaryAdapter().marshal(sha1.digest()).toLowerCase()
        }
    }
}