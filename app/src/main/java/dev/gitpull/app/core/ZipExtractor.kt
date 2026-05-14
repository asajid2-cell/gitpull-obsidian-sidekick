package dev.gitpull.app.core

import java.io.File
import java.util.zip.ZipInputStream

class ZipExtractor {
    fun extractStrippingRoot(zipFile: File, destinationDir: File): File {
        if (destinationDir.exists()) destinationDir.deleteRecursively()
        destinationDir.mkdirs()
        val canonicalDestination = destinationDir.canonicalFile
        ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val strippedName = stripArchiveRoot(entry.name)
                if (strippedName.isBlank()) {
                    zip.closeEntry()
                    continue
                }
                val output = File(destinationDir, strippedName).canonicalFile
                require(output.path.startsWith(canonicalDestination.path + File.separator)) {
                    "Blocked unsafe zip entry: ${entry.name}"
                }
                if (entry.isDirectory) {
                    output.mkdirs()
                } else {
                    output.parentFile?.mkdirs()
                    output.outputStream().use { zip.copyTo(it) }
                }
                zip.closeEntry()
            }
        }
        return destinationDir
    }

    private fun stripArchiveRoot(path: String): String {
        val normalized = path.replace('\\', '/').trim('/')
        val slash = normalized.indexOf('/')
        return if (slash < 0) "" else normalized.substring(slash + 1)
    }
}
