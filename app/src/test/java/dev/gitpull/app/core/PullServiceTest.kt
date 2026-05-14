package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PullServiceTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun pullDownloadsExtractsSyncsDeletesAndIndexesPdfs() {
        val zip = createZip(
            "repo-main/Index.md" to "# Index",
            "repo-main/Papers/One.pdf" to "%PDF"
        )
        val destination = temp.newFolder("destination")
        File(destination, "deleted.md").writeText("remove me")
        val service = PullService(archiveDownloader = { _, out -> zip.copyTo(out, overwrite = true) })

        val report = service.pull(
            PullConfig(RepoRef("owner", "repo"), token = null, cacheDir = temp.newFolder("cache")),
            FileSnapshotWriter(destination)
        )

        assertEquals("# Index", File(destination, "Index.md").readText())
        assertFalse(File(destination, "deleted.md").exists())
        assertEquals(listOf("Papers/One.pdf"), report.pdfs.map { it.relativePath })
        assertEquals(setOf("Index.md", "Papers/One.pdf"), report.manifest.files.keys)
        assertEquals(2, report.filesCopied)
        assertEquals(1, report.filesDeleted)
    }

    @Test
    fun failedDownloadPreservesPreviousSnapshot() {
        val destination = temp.newFolder("destination")
        File(destination, "Index.md").writeText("old")
        val service = PullService(archiveDownloader = { _, _ -> error("network failed") })

        val result = runCatching {
            service.pull(
                PullConfig(RepoRef("owner", "repo"), token = null, cacheDir = temp.newFolder("cache")),
                FileSnapshotWriter(destination)
            )
        }

        assertFalse(result.isSuccess)
        assertEquals("old", File(destination, "Index.md").readText())
    }

    @Test
    fun pullRefreshesCompleteVaultFixtureShape() {
        val zip = createZip(
            "vault-main/Index.md" to "# Index\n![[Assets/diagram.png]]\n[[Papers/One.pdf]]",
            "vault-main/.obsidian/app.json" to """{"legacyEditor":false}""",
            "vault-main/.obsidian/graph.json" to """{"collapse-filter":true}""",
            "vault-main/Assets/diagram.png" to "png-bytes",
            "vault-main/Papers/One.pdf" to "%PDF-1.4\n%%EOF\n"
        )
        val destination = temp.newFolder("complete-vault")
        val service = PullService(archiveDownloader = { _, out -> zip.copyTo(out, overwrite = true) })

        val report = service.pull(
            PullConfig(RepoRef("owner", "vault"), token = "token", cacheDir = temp.newFolder("cache")),
            FileSnapshotWriter(destination)
        )

        assertEquals("# Index\n![[Assets/diagram.png]]\n[[Papers/One.pdf]]", File(destination, "Index.md").readText())
        assertEquals("""{"legacyEditor":false}""", File(destination, ".obsidian/app.json").readText())
        assertTrue(File(destination, "Assets/diagram.png").exists())
        assertEquals(listOf("Papers/One.pdf"), report.pdfs.map { it.relativePath })
        assertEquals(
            setOf("Index.md", ".obsidian/app.json", ".obsidian/graph.json", "Assets/diagram.png", "Papers/One.pdf"),
            report.manifest.files.keys
        )
        assertEquals(5, report.filesCopied)
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
