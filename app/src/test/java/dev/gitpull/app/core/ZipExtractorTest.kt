package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipExtractorTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun extractsAndStripsArchiveRoot() {
        val zip = createZip(
            "vault-main/Index.md" to "# Index",
            "vault-main/folder/Paper.pdf" to "%PDF"
        )
        val destination = temp.newFolder("extract")

        ZipExtractor().extractStrippingRoot(zip, destination)

        assertEquals("# Index", File(destination, "Index.md").readText())
        assertEquals("%PDF", File(destination, "folder/Paper.pdf").readText())
    }

    @Test
    fun blocksZipSlipEntries() {
        val zip = createZip("vault-main/../escape.md" to "bad")
        val destination = temp.newFolder("extract-slip")

        assertTrue(runCatching { ZipExtractor().extractStrippingRoot(zip, destination) }.isFailure)
    }

    private fun createZip(vararg entries: Pair<String, String>): File {
        val file = temp.newFile("archive.zip")
        ZipOutputStream(file.outputStream()).use { zip ->
            for ((name, contents) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(contents.toByteArray())
                zip.closeEntry()
            }
        }
        return file
    }
}
