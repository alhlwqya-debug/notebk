package com.ahd.notebk.data.report

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ahd.notebk.ui.components.NotebkBrand
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max

/**
 * Builds a paginated PDF from the rendered report WebView and shares it as a real PDF file.
 * Uses PdfDocument directly to remain compatible with Android SDK stubs that hide the
 * PrintDocumentAdapter callback constructors in some IDE/build environments.
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
    val safeName = jobName
        .replace(Regex("[^\\p{L}\\p{N}_-]+"), "_")
        .trim('_')
        .ifBlank { "report" }

    webView.post {
        var finished = false
        fun finishOnce() {
            if (!finished) {
                finished = true
                runCatching { onFinished() }
            }
        }

        fun fail(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            finishOnce()
        }

        var pdf: PdfDocument? = null
        val originalLayoutParams = webView.layoutParams
        val originalHeight = webView.height
        val originalScrollY = webView.scrollY
        try {
            check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                "إنشاء PDF يتطلب Android 4.4 أو أحدث"
            }
            val sourceWidth = webView.width
            check(sourceWidth > 0 && originalHeight > 0) {
                "صفحة التقرير غير جاهزة للإنشاء"
            }

            // WebView.height is only the visible viewport. contentHeight represents the full HTML.
            val density = webView.resources.displayMetrics.density
            val contentHeight = (webView.contentHeight * density).toInt()
            val fullHeight = max(originalHeight, contentHeight)
            check(fullHeight > 0) { "محتوى التقرير فارغ" }

            // Temporarily lay out the WebView at the full document height before drawing.
            webView.layoutParams?.let {
                it.height = fullHeight
                webView.layoutParams = it
            }
            val widthSpec = View.MeasureSpec.makeMeasureSpec(sourceWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(fullHeight, View.MeasureSpec.EXACTLY)
            webView.measure(widthSpec, heightSpec)
            webView.layout(0, 0, sourceWidth, fullHeight)
            webView.scrollTo(0, 0)

            val directory = File(context.cacheDir, "shared_reports")
            if (!directory.exists()) check(directory.mkdirs()) {
                "تعذر إنشاء مجلد التقرير"
            }
            val pdfFile = File(directory, "${safeName}_${System.currentTimeMillis()}.pdf")

            val pageWidth = if (landscape) 842 else 595
            val pageHeight = if (landscape) 595 else 842
            val margin = 24
            val printableWidth = pageWidth - margin * 2
            val printableHeight = pageHeight - margin * 2
            val scale = printableWidth.toFloat() / sourceWidth.toFloat()
            val sourcePageHeight = max(1, (printableHeight / scale).toInt())
            val pageCount = ceil(fullHeight.toDouble() / sourcePageHeight.toDouble()).toInt()
            check(pageCount in 1..500) { "عدد صفحات التقرير غير صالح: $pageCount" }

            val document = PdfDocument()
            pdf = document
            for (pageIndex in 0 until pageCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageIndex + 1
                ).create()
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas
                val startY = pageIndex * sourcePageHeight
                val endY = minOf(fullHeight, startY + sourcePageHeight)

                canvas.save()
                canvas.translate(margin.toFloat(), margin.toFloat())
                canvas.scale(scale, scale)
                canvas.clipRect(0, 0, sourceWidth, sourcePageHeight)
                canvas.translate(0f, -startY.toFloat())
                webView.draw(canvas)
                canvas.restore()
                document.finishPage(page)
            }

            FileOutputStream(pdfFile).use { output ->
                document.writeTo(output)
                output.flush()
            }
            document.close()
            pdf = null

            check(pdfFile.isFile && pdfFile.length() > 0L) { "ملف PDF فارغ" }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                pdfFile
            )
            val documentName = jobName.ifBlank { "تقرير" }
            val shareText = buildString {
                append("تقرير: ")
                append(documentName)
                if (phone.isNotBlank()) append("\\nالهاتف: $phone")
                if (email.isNotBlank()) append("\\nالبريد الإلكتروني: $email")
                append("\\n")
                append(NotebkBrand.APP_NAME)
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, documentName)
                putExtra(Intent.EXTRA_TEXT, shareText)
                clipData = ClipData.newUri(context.contentResolver, pdfFile.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "مشاركة التقرير").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            finishOnce()
        } catch (e: Exception) {
            runCatching { pdf?.close() }
            fail("تعذرت مشاركة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}")
        } finally {
            // Restore the original on-screen WebView geometry after the PDF is built.
            runCatching {
                if (originalLayoutParams != null) {
                    originalLayoutParams.height = originalHeight
                    webView.layoutParams = originalLayoutParams
                }
                webView.measure(
                    View.MeasureSpec.makeMeasureSpec(webView.width.coerceAtLeast(1), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(originalHeight.coerceAtLeast(1), View.MeasureSpec.EXACTLY)
                )
                webView.layout(
                    webView.left,
                    webView.top,
                    webView.left + webView.width,
                    webView.top + originalHeight.coerceAtLeast(1)
                )
                webView.scrollTo(0, originalScrollY)
            }
        }
    }
}
