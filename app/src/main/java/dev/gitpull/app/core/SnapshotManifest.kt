package dev.gitpull.app.core

import java.io.File
import java.security.MessageDigest

data class SnapshotManifest(
    val files: Map<String, FileFingerprint>
)

data class FileFingerprint(
    val size: Long,
    val sha256: String
)

data class LocalChangeSet(
    val added: Set<String>,
    val modified: Set<String>,
    val deleted: Set<String>
) {
    val hasChanges: Boolean = added.isNotEmpty() || modified.isNotEmpty() || deleted.isNotEmpty()
    val summary: String = listOfNotNull(
        added.takeIf { it.isNotEmpty() }?.let { "${it.size} added" },
        modified.takeIf { it.isNotEmpty() }?.let { "${it.size} modified" },
        deleted.takeIf { it.isNotEmpty() }?.let { "${it.size} deleted" }
    ).joinToString(", ")
}

class FileSnapshotManifestBuilder {
    fun build(root: File): SnapshotManifest {
        if (!root.exists()) return SnapshotManifest(emptyMap())
        val files = root.walkTopDown()
            .filter { it.isFile }
            .associate { file ->
                file.relativeTo(root).invariantSeparatorsPath to FileFingerprint(
                    size = file.length(),
                    sha256 = sha256(file.readBytes())
                )
            }
        return SnapshotManifest(files)
    }

    companion object {
        fun sha256(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}

object SnapshotManifestComparator {
    fun localChanges(lastPulled: SnapshotManifest, current: SnapshotManifest): LocalChangeSet {
        val added = current.files.keys - lastPulled.files.keys
        val deleted = lastPulled.files.keys - current.files.keys
        val modified = current.files.keys.intersect(lastPulled.files.keys)
            .filter { current.files[it] != lastPulled.files[it] }
            .toSet()
        return LocalChangeSet(added = added, modified = modified, deleted = deleted)
    }
}

object SnapshotManifestCodec {
    fun encode(manifest: SnapshotManifest): String {
        return manifest.files.entries
            .sortedBy { it.key }
            .joinToString("\n") { (path, fingerprint) ->
                "${escape(path)}\t${fingerprint.size}\t${fingerprint.sha256}"
            }
    }

    fun decode(encoded: String): SnapshotManifest {
        if (encoded.isBlank()) return SnapshotManifest(emptyMap())
        val files = encoded.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split('\t')
                if (parts.size != 3) return@mapNotNull null
                unescape(parts[0]) to FileFingerprint(parts[1].toLong(), parts[2])
            }
            .toMap()
        return SnapshotManifest(files)
    }

    private fun escape(value: String): String {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")
    }

    private fun unescape(value: String): String {
        val out = StringBuilder()
        var escaped = false
        for (char in value) {
            if (escaped) {
                out.append(
                    when (char) {
                        't' -> '\t'
                        'n' -> '\n'
                        else -> char
                    }
                )
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else {
                out.append(char)
            }
        }
        return out.toString()
    }
}
