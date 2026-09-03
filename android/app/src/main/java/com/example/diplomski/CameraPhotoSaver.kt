package com.example.diplomski

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object CameraPhotoSaver {

    private const val TAG = "CameraPhotoSaver"
    private const val FILENAMEFORMAT = "yyyy-MM-dd_HH-mm-ss-SSS"
    private const val PHOTO_EXTENSION = ".jpg"


    fun savePictureToFile(context: Context, bitmap: Bitmap?, mimeType: String, name: String): String {
        if (bitmap == null) return ""

        val outputDirectory = getOutputMediaDirectory(context)
        var photoFile = createFileWithoutName(outputDirectory, FILENAMEFORMAT, PHOTO_EXTENSION)
        if (name != "")
            photoFile = createFile(outputDirectory, name, PHOTO_EXTENSION)

        FileOutputStream(photoFile).use { stream ->
            when {
                mimeType.contains("jpg", true) || mimeType.contains(
                    "jpeg", true
                ) -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                mimeType.contains("png", true) -> bitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, stream
                )
            }
        }

        Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
        updateMedia(context, Uri.fromFile(photoFile))
        return photoFile.absolutePath
    }

    private fun updateMedia(context: Context, savedUri: Uri) {
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(savedUri.toFile().extension)

        MediaScannerConnection.scanFile(
            context, arrayOf(savedUri.toFile().absolutePath), arrayOf(mimeType)
        ) { path, uri ->
            Log.d(TAG, if (uri != null) "Scan succeeded: $path" else "Scan failed: $path")
        }
    }

    private fun getOutputFileOptions(
        lensFacing: Int?, photoFile: File
    ): ImageCapture.OutputFileOptions {
        return if (lensFacing == null) {
            ImageCapture.OutputFileOptions.Builder(photoFile).build()
        } else {
            val metadata = ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }
            ImageCapture.OutputFileOptions.Builder(photoFile).setMetadata(metadata).build()
        }
    }

    fun getOutputMediaDirectory(context: Context): File {
        return context.filesDir.apply { mkdirs() }
    }

    fun getOutputFileDirectory(context: Context): File =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    fun createFileWithoutName(
        storageDir: File, format: String = FILENAMEFORMAT, extension: String = PHOTO_EXTENSION
    ): File = File(
        storageDir,
        SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension
    )

    fun createFile(
        storageDir: File, name: String, extension: String = PHOTO_EXTENSION
    ): File = File(
        storageDir,
        name + extension
    )

    fun createTempFile(
        storageDir: File, format: String = FILENAMEFORMAT, extension: String = PHOTO_EXTENSION
    ): File = File.createTempFile(
        SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()),
        extension,
        storageDir
    )

    fun loadBitmapFromUri(context: Context, uri: Uri): ImageBitmap? {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver, uri
                ).asImageBitmap()
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source).asImageBitmap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading bitmap from: $uri", e)
            null
        }
    }

    fun copyFileToInternalStorage(context: Context, uri: Uri, fileName: String): String {
        val destFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
    }
    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = cursor.getString(index)
            }
        }
        return name
    }

    fun openPdf(context: Context, path: String) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
