package dev.gitpull.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

object AndroidSnapshotTools {
    fun clearTree(context: Context, treeUri: String): Int {
        if (treeUri.isBlank()) return 0
        val uri = Uri.parse(treeUri)
        if (uri.scheme == "file") {
            val root = File(requireNotNull(uri.path))
            val count = root.walkBottomUp().count { it != root }
            root.deleteRecursively()
            return count
        }
        val root = DocumentFile.fromTreeUri(context, uri) ?: return 0
        var deleted = 0
        for (child in root.listFiles()) {
            if (child.delete()) deleted += 1
        }
        return deleted
    }
}
