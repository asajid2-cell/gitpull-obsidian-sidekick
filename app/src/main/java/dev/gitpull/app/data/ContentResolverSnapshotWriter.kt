package dev.gitpull.app.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dev.gitpull.app.core.SnapshotWriteReport
import dev.gitpull.app.core.SnapshotWriter
import java.io.File

class ContentResolverSnapshotWriter(
    context: Context,
    private val contentResolver: ContentResolver,
    destinationTreeUri: Uri
) : SnapshotWriter {
    private val root = requireNotNull(DocumentFile.fromTreeUri(context, destinationTreeUri)) {
        "Could not open destination tree"
    }

    override fun refreshFrom(sourceRoot: File): SnapshotWriteReport {
        val sourceFiles = sourceRoot.walkTopDown()
            .filter { it.isFile }
            .associateBy { it.relativeTo(sourceRoot).invariantSeparatorsPath }

        var copied = 0
        var deleted = deleteMissing(root, "", sourceFiles.keys)

        for ((relativePath, sourceFile) in sourceFiles) {
            writeFile(relativePath, sourceFile)
            copied += 1
        }

        return SnapshotWriteReport(copied = copied, deleted = deleted)
    }

    private fun deleteMissing(directory: DocumentFile, prefix: String, sourcePaths: Set<String>): Int {
        var deleted = 0
        for (child in directory.listFiles()) {
            val relative = if (prefix.isBlank()) child.name.orEmpty() else "$prefix/${child.name.orEmpty()}"
            if (child.isDirectory) {
                deleted += deleteMissing(child, relative, sourcePaths)
                if (child.listFiles().isEmpty()) child.delete()
            } else if (relative !in sourcePaths) {
                if (child.delete()) deleted += 1
            }
        }
        return deleted
    }

    private fun writeFile(relativePath: String, sourceFile: File) {
        val parts = relativePath.split('/').filter { it.isNotBlank() }
        var directory = root
        for (part in parts.dropLast(1)) {
            directory = directory.findFile(part) ?: directory.createDirectory(part)
                ?: error("Could not create directory $part")
        }
        val filename = parts.last()
        val target = directory.findFile(filename)
            ?: directory.createFile(mimeTypeFor(filename), filename)
            ?: error("Could not create file $filename")
        contentResolver.openOutputStream(target.uri, "wt")?.use { out ->
            sourceFile.inputStream().use { it.copyTo(out) }
        } ?: error("Could not write $relativePath")
    }

    private fun mimeTypeFor(filename: String): String {
        return when (filename.substringAfterLast('.', "").lowercase()) {
            "md" -> "text/markdown"
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }
}
