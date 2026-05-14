package dev.gitpull.app.core

import java.io.File

interface SnapshotWriter {
    fun refreshFrom(sourceRoot: File): SnapshotWriteReport
}

data class SnapshotWriteReport(
    val copied: Int,
    val deleted: Int
)

class FileSnapshotWriter(private val destinationRoot: File) : SnapshotWriter {
    override fun refreshFrom(sourceRoot: File): SnapshotWriteReport {
        require(sourceRoot.isDirectory) { "sourceRoot must be a directory" }
        destinationRoot.mkdirs()
        val sourceRelative = sourceRoot.walkTopDown()
            .filter { it.isFile }
            .associateBy { it.relativeTo(sourceRoot).invariantSeparatorsPath }

        var copied = 0
        var deleted = 0

        for ((relativePath, sourceFile) in sourceRelative) {
            val destinationFile = File(destinationRoot, relativePath)
            val needsCopy = !destinationFile.exists() ||
                destinationFile.length() != sourceFile.length() ||
                destinationFile.readBytes().contentEquals(sourceFile.readBytes()).not()
            if (needsCopy) {
                destinationFile.parentFile?.mkdirs()
                sourceFile.copyTo(destinationFile, overwrite = true)
                copied += 1
            }
        }

        if (destinationRoot.exists()) {
            destinationRoot.walkBottomUp()
                .filter { it != destinationRoot }
                .forEach { existing ->
                    val relative = existing.relativeTo(destinationRoot).invariantSeparatorsPath
                    if (existing.isFile && relative !in sourceRelative.keys) {
                        existing.delete()
                        deleted += 1
                    } else if (existing.isDirectory && existing.list()?.isEmpty() == true) {
                        existing.delete()
                    }
                }
        }

        return SnapshotWriteReport(copied = copied, deleted = deleted)
    }
}
