package dev.gitpull.app.core

import java.io.File

class PdfIndexer {
    fun indexFiles(root: File): List<PdfItem> {
        if (!root.exists()) return emptyList()
        return root.walkTopDown()
            .filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            .map {
                PdfItem(
                    filename = it.name,
                    relativePath = it.relativeTo(root).invariantSeparatorsPath,
                    uriString = it.toURI().toString()
                )
            }
            .sortedWith(compareBy<PdfItem> { it.filename.lowercase() }.thenBy { it.relativePath.lowercase() })
            .toList()
    }
}
