package com.openviewer.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

/** One openable file type, e.g. ".STEP". */
data class FileType(
    val label: String,
    val extensions: List<String>,
    val viewer: Class<out Activity>,
)

/** A group of file types, e.g. "3D". */
data class FileCategory(val name: String, val types: List<FileType>)

/**
 * Registry of every file type open_viewer can open.
 * To add a new viewer: create its Activity and add a FileType here.
 */
object FileTypes {
    val categories: List<FileCategory> = listOf(
        FileCategory(
            "3D",
            listOf(
                FileType(".STEP", listOf("step", "stp"), StepViewerActivity::class.java),
            ),
        ),
    )

    fun forFileName(name: String?): FileType? {
        val ext = name?.substringAfterLast('.', "")?.lowercase() ?: return null
        return categories.flatMap { it.types }.firstOrNull { ext in it.extensions }
    }

    fun viewerIntent(context: Context, type: FileType, uri: Uri, name: String): Intent =
        Intent(context, type.viewer)
            .setData(uri)
            .putExtra(EXTRA_NAME, name)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    const val EXTRA_NAME = "com.openviewer.app.NAME"
}
