package com.example.diplomski.elements

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import java.io.File
import androidx.core.graphics.createBitmap


@Composable
fun PdfViewer(path: String) {

    val rendererState = remember(path) {
        PdfRendererState(path)
    }
    val renderer = remember(path) {
        PdfRenderer(
            ParcelFileDescriptor.open(
                File(path),
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose { renderer.close() }
    }
    val cache = remember { mutableStateMapOf<Int, Bitmap>() }

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        items(renderer.pageCount) { index ->
            if (!cache.containsKey(index)) {
                cache[index] = rendererState.renderPage(index)
            }

            cache[index]?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Image(
                        bitmap = cache[index]!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
            }
        }
    }
}

class PdfRendererState(
    pdfPath: String
) {
    private val file =
        ParcelFileDescriptor.open(File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(file)
    fun renderPage(index: Int): Bitmap {

        val page = renderer.openPage(index)
        val bitmap = createBitmap(page.width, page.height)
        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )
        page.close()
        return bitmap
    }

    fun close() {
        renderer.close()
        file.close()
    }
}