package com.ahd.notebk.data.report

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ahd.notebk.ui.components.NotebkBrand
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Creates a PDF directly from the currently rendered WebView and opens the share sheet.
 * It captures the visible WebView viewport page-by-page instead of expanding the WebView
 * to the entire document, which can cause clipping and excessive memory usage.
 */
internal fun shareWebViewAsPdf(
    context: Context,
    webView: WebView,
    jobName: String,
    phone: String,
    email: String,
    onFinished: () -> Unit,
    landscape: Boolean = false
) {
    webView.post {
        var document: PdfDocument? = null
        val oldScrollY = webView.scrollY
        val oldLayerType = webView.layerType
        try {
            check(webView.width > 0 && webView.height > 0) {
                "صفحة التقرير غير جاهزة للإنشاء"
            }

            val sourceWidth = webView.width
            val viewportHeight = webView.height
            val density = webView.resources.displayMetrics.density
            val contentHeight = max(viewportHeight, (webView.contentHeight * density).toInt())
            val pageWidth = if (landscape) 842 else 595
            val pageHeight = if (landscape) 595 else 842
            val margin = 24
            val printableWidth = pageWidth - margin * 2
            val printableHeight = pageHeight - margin * 2
            val scale = printableWidth.toFloat() / sourceWidth.toFloat()

            // Never request more source pixels than WebView can actually draw in one viewport.
            val sourcePageHeight = max(
                1,
                min(viewportHeight, (printableHeight / scale).toInt())
            )
            val pageCount = ceil(contentHeight.toDouble() / sourcePageHeight.toDouble()).toInt()
            check(pageCount in 1..500) { "عدد صفحات التقرير غير صالح: $pageCount" }

            val safeName = jobName
                .replace(Regex("[^\\p{L}\\p{N}_-]+"), "_")
                .trim('_')
                .ifBlank { "report" }
            val directory = File(context.cacheDir, "shared_reports")
            if (!directory.exists()) check(directory.mkdirs()) { "تعذر إنشاء مجلد التقرير" }
            val pdfFile = File(directory, "${safeName}_${System.currentTimeMillis()}.pdf")

            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            val pdf = PdfDocument()
            document = pdf

            for (index in 0 until pageCount) {
                val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdf.startPage(info)
                val canvas: Canvas = page.canvas
                val startY = index * sourcePageHeight

                webView.scrollTo(0, startY)
                webView.invalidate()

                canvas.save()
                canvas.translate(margin.toFloat(), margin.toFloat())
                canvas.scale(scale, scale)
                canvas.clipRect(0, 0, sourceWidth, sourcePageHeight)
                webView.draw(canvas)
                canvas.restore()
                pdf.finishPage(page)
            }

            FileOutputStream(pdfFile).use { output ->
                pdf.writeTo(output)
                output.flush()
            }
            pdf.close()
            document = null

            check(pdfFile.isFile && pdfFile.length() > 0L) { "تعذر إنشاء ملف PDF" }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                pdfFile
            )
            val shareTitle = jobName.ifBlank { "تقرير" }
            val shareText = buildString {
                append("تقرير: ")
                append(shareTitle)
                if (phone.isNotBlank()) append("\nالهاتف: $phone")
                if (email.isNotBlank()) append("\nالبريد الإلكتروني: $email")
                append("\n")
                append(NotebkBrand.APP_NAME)
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                putExtra(Intent.EXTRA_TEXT, shareText)
                clipData = ClipData.newUri(context.contentResolver, pdfFile.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "مشاركة التقرير").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "تعذرت مشاركة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            runCatching { document?.close() }
            runCatching { webView.scrollTo(0, oldScrollY) }
            runCatching { webView.setLayerType(oldLayerType, null) }
            runCatching { onFinished() }
        }
    }
}
