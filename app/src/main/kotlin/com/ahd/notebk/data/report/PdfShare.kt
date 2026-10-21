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

/**
 * Creates a PDF from the current WebView report and opens Android's share sheet.
 * The FileProvider authority must be "${'$'}{applicationId}.fileprovider".
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

    // WebView measurement and drawing must happen on its UI thread.
    webView.post {
        val originalWidth = webView.width
        val originalHeight = webView.height
        val originalLayoutParams = webView.layoutParams
        val originalLeft = webView.left
        val originalTop = webView.top

        try {
            check(originalWidth > 0) { "تعذر تحديد عرض التقرير" }
            check(originalHeight > 0) { "تعذر تحديد ارتفاع التقرير" }

            val pageWidth = if (landscape) 842 else 595
            val pageHeight = if (landscape) 595 else 842
            val margin = 24
            val printableWidth = pageWidth - margin * 2
            val printableHeight = pageHeight - margin * 2

            val contentHeight = max(
                originalHeight,
                ceil(webView.contentHeight * webView.scale).toInt()
            ).coerceAtLeast(1)

            webView.measure(
                View.MeasureSpec.makeMeasureSpec(originalWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(contentHeight, View.MeasureSpec.EXACTLY)
            )
            webView.layout(
                originalLeft,
                originalTop,
                originalLeft + originalWidth,
                originalTop + contentHeight
            )

            val scale = printableWidth.toFloat() / originalWidth.toFloat()
            val contentHeightPerPage = printableHeight.toFloat() / scale
            val pageCount = max(1, ceil(contentHeight / contentHeightPerPage).toInt())

            val pdf = PdfDocument()
            val pdfFile: File
            try {
                for (pageIndex in 0 until pageCount) {
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        pageIndex + 1
                    ).create()
                    val page = pdf.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    canvas.save()
                    canvas.translate(margin.toFloat(), margin.toFloat())
                    canvas.scale(scale, scale)
                    canvas.translate(0f, -(pageIndex * contentHeightPerPage))
                    webView.draw(canvas)
                    canvas.restore()

                    pdf.finishPage(page)
                }

                val reportDirectory = File(context.cacheDir, "shared_reports")
                if (!reportDirectory.exists() && !reportDirectory.mkdirs()) {
                    throw IllegalStateException("تعذر إنشاء مجلد التقرير")
                }

                pdfFile = File(
                    reportDirectory,
                    "${'$'}{safeName}_${'$'}{System.currentTimeMillis()}.pdf"
                )
                FileOutputStream(pdfFile).use { output -> pdf.writeTo(output) }
            } finally {
                pdf.close()
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${'$'}{context.packageName}.fileprovider",
                pdfFile
            )

            val shareText = buildString {
                append("تقرير: ")
                append(jobName.ifBlank { "تقرير" })
                if (phone.isNotBlank()) {
                    append("\nالهاتف: ")
                    append(phone)
                }
                if (email.isNotBlank()) {
                    append("\nالبريد الإلكتروني: ")
                    append(email)
                }
                append("\n")
                append(NotebkBrand.APP_NAME)
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, jobName.ifBlank { "تقرير" })
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
                "تعذرت مشاركة التقرير: ${'$'}{e.localizedMessage ?: "خطأ غير معروف"}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            // Restore the WebView layout even if PDF generation or sharing fails.
            try {
                webView.layoutParams = originalLayoutParams
                if (originalWidth > 0 && originalHeight > 0) {
                    webView.measure(
                        View.MeasureSpec.makeMeasureSpec(originalWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(originalHeight, View.MeasureSpec.EXACTLY)
                    )
                    webView.layout(
                        originalLeft,
                        originalTop,
                        originalLeft + originalWidth,
                        originalTop + originalHeight
                    )
                }
            } catch (_: Exception) {
                // Best-effort restoration.
            }
            onFinished()
        }
    }
}
