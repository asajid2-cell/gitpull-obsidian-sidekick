package dev.gitpull.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dev.gitpull.app.core.FileFingerprint
import dev.gitpull.app.core.FileSnapshotManifestBuilder
import dev.gitpull.app.core.SnapshotManifest
import java.io.File

class AndroidSnapshotManifestBuilder(private val context: Context) {
    fun build(treeUri: String): SnapshotManifest {
        if (treeUri.isBlank()) return SnapshotManifest(emptyMap())
        val uri = Uri.parse(treeUri)
        if (uri.scheme == "file") return FileSnapshotManifestBuilder().build(File(requireNotNull(uri.path)))
        if (uri.scheme != "content") return SnapshotManifest(emptyMap())
        val root = DocumentFile.fromTreeUri(context, uri) ?: return SnapshotManifest(emptyMap())
        val files = mutableMapOf<String, FileFingerprint>()
        collect(root, "", files)
        return SnapshotManifest(files)
    }

    private fun collect(file: DocumentFile, prefix: String, output: MutableMap<String, FileFingerprint>) {
        for (child in file.listFiles()) {
            val name = child.name.orEmpty()
            val relative = if (prefix.isBlank()) name else "$prefix/$name"
            when {
                child.isDirectory -> collect(child, relative, output)
                child.isFile -> {
                    val bytes = context.contentResolver.openInputStream(child.uri)?.use { it.readBytes() } ?: ByteArray(0)
                    output[relative] = FileFingerprint(size = bytes.size.toLong(), sha256 = FileSnapshotManifestBuilder.sha256(bytes))
                }
            }
        }
    }
}
