package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PdfIndexerTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun findsNestedPdfsOnly() {
        val root = temp.newFolder("vault")
        File(root, "notes").mkdir()
        File(root, "notes/Paper.pdf").writeText("pdf")
        File(root, "notes/Note.md").writeText("note")
        File(root, "Root.PDF").writeText("pdf")

        val pdfs = PdfIndexer().indexFiles(root)

        assertEquals(listOf("notes/Paper.pdf", "Root.PDF").sorted(), pdfs.map { it.relativePath }.sorted())
    }
}
