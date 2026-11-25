package fr.uha.hassenforder.easy.sudoku.provider

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import fr.uha.hassenforder.easy.android.provider.DocumentProvider
import fr.uha.hassenforder.easy.sudoku.R

class SudokuFileProvider : FileProvider(
    R.xml.file_paths
) {
    companion object {

        fun getAssetDocumentUri(filename: String): Uri {
            return DocumentProvider.getAssetDocumentUri(filename)
        }

        fun getResourceDocumentUri(context: Context, id: Int): Uri {
            return DocumentProvider.getResourceDocumentUri(context, id)
        }

        fun createExternalImageFilename(
            prefix: String = "photo",
            extension: String? = "jpg"
        ): String {
            return DocumentProvider.createExternalImageFilename(prefix, extension)
        }

        fun createExternalVideoFilename(
            prefix: String = "video",
            extension: String? = "mp4"
        ): String {
            return DocumentProvider.createExternalVideoFilename(prefix, extension)
        }

        fun createExternalImageUri(
            context: Context,
            prefix : String = "photo",
            extension : String? = "jpg"
        ): Uri {
            return DocumentProvider.createExternalImageUri(context, prefix, extension)
        }

        fun createExternalMovieUri(
            context: Context,
            prefix : String = "video",
            extension : String? = "mp4"
        ): Uri {
            return DocumentProvider.createExternalMovieUri(context, prefix, extension)
        }

    }
}