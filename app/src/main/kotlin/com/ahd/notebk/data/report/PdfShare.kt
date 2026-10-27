package com.ahd.notebk.data.report

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ahd.notebk.ui.components.NotebkBrand
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max

/**
 * Creates a real multi-page PDF from the already-rendered report WebView and shares
 * it as an application/pdf attachment. The report HTML/data generation is deliberately
 * left untouched.
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
                onFinished()
            }
        }

        var document: PdfDocument? = null
        try {
            check(webView.width > 0 && webView.height > 0) {
                "صفحة التقرير غير جاهزة للإنشاء"
            }

            val directory = File(context.cacheDir, "shared_reports")
            if (!directory.exists()) check(directory.mkdirs()) {
                "تعذر إنشاء مجلد التقرير"
            }
            val pdfFile = File(directory, "${safeName}_${System.currentTimeMillis()}.pdf")

            // Capture the existing WebView content; do not rebuild or truncate the report HTML.
            val picture: Picture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.capturePicture()
            } else {
                webView.capturePicture()
            }
            val sourceWidth = picture.width
            val sourceHeight = picture.height
            check(sourceWidth > 0 && sourceHeight > 0) { "محتوى التقرير فارغ" }

            val pageWidth = if (landscape) 842 else 595
            val pageHeight = if (landscape) 595 else 842
            val margin = 24
            val printableWidth = pageWidth - margin * 2
            val printableHeight = pageHeight - margin * 2
            val scale = printableWidth.toFloat() / sourceWidth.toFloat()
            val sourcePageHeight = max(1, (printableHeight / scale).toInt())
            val pageCount = ceil(sourceHeight.toDouble() / sourcePageHeight.toDouble()).toInt()
            check(pageCount in 1..500) { "عدد صفحات التقرير غير صالح: $pageCount" }

            val pdf = PdfDocument()
            document = pdf
            for (pageIndex in 0 until pageCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdf.startPage(pageInfo)
                val canvas: Canvas = page.canvas
                canvas.save()
                canvas.translate(margin.toFloat(), margin.toFloat())
                canvas.scale(scale, scale)
                canvas.translate(0f, -(pageIndex * sourcePageHeight).toFloat())
                canvas.clipRect(
                    0,
                    pageIndex * sourcePageHeight,
                    sourceWidth,
                    (pageIndex + 1) * sourcePageHeight
                )
                picture.draw(canvas)
                canvas.restore()
                pdf.finishPage(page)
            }

            FileOutputStream(pdfFile).use { output ->
                pdf.writeTo(output)
                output.flush()
            }
            pdf.close()
            document = null

            check(pdfFile.isFile && pdfFile.length() > 0L) { "ملف PDF فارغ" }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                pdfFile
            )
            val shareText = buildString {
                append("تقرير: ")
                append(jobName.ifBlank { "تقرير" })
                if (phone.isNotBlank()) append("\\nالهاتف: $phone")
                if (email.isNotBlank()) append("\\nالبريد الإلكتروني: $email")
                append("\\n")
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
            runCatching { document?.close() }
            Toast.makeText(
                context,
                "تعذرت مشاركة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            finishOnce()
        }
    }
}
