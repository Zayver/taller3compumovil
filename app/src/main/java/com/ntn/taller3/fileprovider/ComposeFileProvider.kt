package com.ntn.taller3.fileprovider

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.ntn.taller3.R
import java.io.File

class ComposeFileProvider: FileProvider(R.xml.provider_paths) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.filesDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}