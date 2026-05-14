package dev.gitpull.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dev.gitpull.app.core.PdfItem
import dev.gitpull.app.core.PdfIndexer
import java.io.File

class AndroidPdfIndexer(private val context: Context) {
    fun indexTree(treeUri: String): List<PdfItem> {
        if (treeUri.isBlank()) return emptyList()
        val uri = Uri.parse(treeUri)
        if (uri.scheme == "file") return PdfIndexer().indexFiles(File(requireNotNull(uri.path)))
        if (uri.scheme != "content") return emptyList()
        val root = DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
        val output = mutableListOf<PdfItem>()
        collect(root, "", output)
        return output.sortedWith(compareBy<PdfItem> { it.filename.lowercase() }.thenBy { it.relativePath.lowercase() })
    }

    private fun collect(file: DocumentFile, prefix: String, output: MutableList<PdfItem>) {
        for (child in file.listFiles()) {
            val name = child.name.orEmpty()
            val relative = if (prefix.isBlank()) name else "$prefix/$name"
            when {
                child.isDirectory -> collect(child, relative, output)
                name.endsWith(".pdf", ignoreCase = true) -> output += PdfItem(
                    filename = name,
                    relativePath = relative,
                    uriString = child.uri.toString()
                )
            }
        }
    }
}
