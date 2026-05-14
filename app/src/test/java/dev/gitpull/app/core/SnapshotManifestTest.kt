package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SnapshotManifestTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun buildsStableManifestWithHashes() {
        val root = temp.newFolder("vault")
        File(root, "Index.md").writeText("# Index")

        val manifest = FileSnapshotManifestBuilder().build(root)

        assertEquals(setOf("Index.md"), manifest.files.keys)
        assertEquals(7, manifest.files.getValue("Index.md").size)
        assertTrue(manifest.files.getValue("Index.md").sha256.isNotBlank())
    }

    @Test
    fun detectsAddedModifiedAndDeletedLocalChanges() {
        val lastPulled = SnapshotManifest(
            mapOf(
                "Index.md" to FileFingerprint(3, "aaa"),
                "Deleted.md" to FileFingerprint(3, "bbb"),
                "Changed.md" to FileFingerprint(3, "ccc")
            )
        )
        val current = SnapshotManifest(
            mapOf(
                "Index.md" to FileFingerprint(3, "aaa"),
                "Changed.md" to FileFingerprint(9, "ddd"),
                "Added.md" to FileFingerprint(3, "eee")
            )
        )

        val changes = SnapshotManifestComparator.localChanges(lastPulled, current)

        assertTrue(changes.hasChanges)
        assertEquals(setOf("Added.md"), changes.added)
        assertEquals(setOf("Changed.md"), changes.modified)
        assertEquals(setOf("Deleted.md"), changes.deleted)
    }

    @Test
    fun codecRoundTripsManifest() {
        val manifest = SnapshotManifest(
            mapOf(
                "folder/One.md" to FileFingerprint(1, "abc"),
                "Two.pdf" to FileFingerprint(2, "def")
            )
        )

        val decoded = SnapshotManifestCodec.decode(SnapshotManifestCodec.encode(manifest))

        assertEquals(manifest, decoded)
        assertFalse(SnapshotManifestComparator.localChanges(manifest, decoded).hasChanges)
    }
}
