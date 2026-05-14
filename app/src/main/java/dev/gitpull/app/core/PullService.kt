package dev.gitpull.app.core

import java.io.File

class PullService(
    private val archiveDownloader: (PullConfig, File) -> File,
    private val extractor: ZipExtractor = ZipExtractor(),
    private val pdfIndexer: PdfIndexer = PdfIndexer(),
    private val manifestBuilder: FileSnapshotManifestBuilder = FileSnapshotManifestBuilder()
) {
    fun pull(config: PullConfig, writer: SnapshotWriter): PullReport {
        require(config.cacheDir.mkdirs() || config.cacheDir.exists()) { "Could not create cache dir" }
        val archive = File(config.cacheDir, "latest.zip")
        val extractDir = File(config.cacheDir, "latest-extracted")
        val downloaded = archiveDownloader(config, archive)
        val sourceRoot = extractor.extractStrippingRoot(downloaded, extractDir)
        val writeReport = writer.refreshFrom(sourceRoot)
        val pdfs = pdfIndexer.indexFiles(sourceRoot)
        val manifest = manifestBuilder.build(sourceRoot)
        return PullReport(
            filesCopied = writeReport.copied,
            filesDeleted = writeReport.deleted,
            pdfs = pdfs,
            message = "Downloaded ${writeReport.copied} files",
            manifest = manifest
        )
    }
}
