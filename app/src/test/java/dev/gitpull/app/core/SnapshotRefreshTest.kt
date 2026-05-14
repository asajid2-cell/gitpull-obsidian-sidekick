package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SnapshotRefreshTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun refreshAddsUpdatesAndDeletesFiles() {
        val source = temp.newFolder("source")
        val destination = temp.newFolder("destination")
        File(destination, "old.md").writeText("old")
        File(destination, "changed.md").writeText("before")
        File(source, "changed.md").writeText("after")
        File(source, "new.md").writeText("new")

        val report = FileSnapshotWriter(destination).refreshFrom(source)

        assertEquals("after", File(destination, "changed.md").readText())
        assertEquals("new", File(destination, "new.md").readText())
        assertFalse(File(destination, "old.md").exists())
        assertEquals(2, report.copied)
        assertEquals(1, report.deleted)
    }
}
