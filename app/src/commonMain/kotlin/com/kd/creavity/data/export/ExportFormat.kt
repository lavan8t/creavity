package com.kd.creavity.data.export

enum class ExportFormat(
    val extension: String,
    val mimeType: String,
    val displayName: String,
    val description: String,
    val iconName: String
) {
    PDF("pdf", "application/pdf", "PDF Document", "Multi-page vector document for sharing or printing", "picture_as_pdf"),
    PNG("png", "image/png", "PNG Image", "High-resolution raster image of the active board", "image"),
    SVG("svg", "image/svg+xml", "SVG Vector", "Scalable vector graphic compatible with Figma & Illustrator", "auto_awesome"),
    CREAVITY("creavity", "application/json", "CreaVITy Backup", "Native lossless whiteboard file for backup and transfer", "folder")
}
