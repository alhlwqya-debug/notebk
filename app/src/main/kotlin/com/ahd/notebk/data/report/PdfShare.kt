package com.ahd.notebk.data.report

import android.content.Context
<<<<<<< HEAD
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.print.PrintManager
=======
import android.content.Intent
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.view.View
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ahd.notebk.ui.components.NotebkBrand
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max

/**
 * Renders the current WebView report to a PDF file and opens Android's
 * native share sheet so the user can share it with WhatsApp, Gmail, Bluetooth,
 * and other compatible apps.
 *
<<<<<<< HEAD
 * Users can select "Save as PDF" from the system print dialog.
 * PrintManager manages the print callbacks.
=======
 * FileProvider is configured in AndroidManifest.xml and res/xml/file_paths.xml.
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
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

    // WebView drawing and measurement must run on its UI thread.
    webView.post {
        var originalWidth = webView.width
        var originalHeight = webView.height
        var originalLayoutParams = webView.layoutParams

        try {
            if (originalWidth <= 0) {
                throw IllegalStateException("تعذر تحديد عرض التقرير")
            }

            val density = context.resources.displayMetrics.density
            val pageWidth = if (landscape) 842 else 595
            val pageHeight = if (landscape) 595 else 842
            val margin = 24
            val printableWidth = pageWidth - margin * 2
            val printableHeight = pageHeight - margin * 2

            // Expand the WebView to its full rendered content before drawing.
            val contentHeight = max(
                originalHeight,
                ceil(webView.contentHeight * webView.scale).toInt()
            )
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                originalWidth,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                contentHeight,
                View.MeasureSpec.EXACTLY
            )
            webView.measure(widthSpec, heightSpec)
            webView.layout(
                webView.left,
                webView.top,
                webView.left + originalWidth,
                webView.top + contentHeight
            )

            val scale = printableWidth.toFloat() / originalWidth.toFloat()
            val contentHeightOnPage = printableHeight / scale
            val pageCount = max(1, ceil(contentHeight / contentHeightOnPage).toInt())

            val pdf = PdfDocument()
            try {
<<<<<<< HEAD
                val printManager = context.getSystemService(
                    Context.PRINT_SERVICE
                ) as PrintManager

                val mediaSize = reportMediaSize(context).let { size ->
                    if (landscape && size.isPortrait) {
                        size.asLandscape()
                    } else if (!landscape && !size.isPortrait) {
                        size.asPortrait()
                    } else {
                        size
                    }
                }

                val attributes = PrintAttributes.Builder()
                    .setMediaSize(mediaSize)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
=======
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
                    canvas.translate(
                        0f,
                        -(pageIndex * contentHeightOnPage)
                    )
                    webView.draw(canvas)
                    canvas.restore()
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk

<<<<<<< HEAD
                val baseAdapter = webView.createPrintDocumentAdapter(safeName)

                val adapter = object : PrintDocumentAdapter() {

                    override fun onStart() {
                        baseAdapter.onStart()
                    }

                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes,
                        cancellationSignal: CancellationSignal,
                        callback: LayoutResultCallback,
                        extras: Bundle?
                    ) {
                        baseAdapter.onLayout(
                            oldAttributes,
                            newAttributes,
                            cancellationSignal,
                            callback,
                            extras
                        )
                    }

                    override fun onWrite(
                        pages: Array<PageRange>,
                        destination: ParcelFileDescriptor,
                        cancellationSignal: CancellationSignal,
                        callback: WriteResultCallback
                    ) {
                        baseAdapter.onWrite(
                            pages,
                            destination,
                            cancellationSignal,
                            callback
                        )
                    }

                    override fun onFinish() {
                        try {
                            baseAdapter.onFinish()
                        } finally {
                            onFinished()
                        }
                    }
=======
                    pdf.finishPage(page)
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
                }

<<<<<<< HEAD
                printManager.print(safeName, adapter, attributes)

                val details = buildString {
                    append("تم فتح نافذة طباعة التقرير.")
                    append(" اختر «حفظ بتنسيق PDF» لإنشاء الملف.")

                    if (phone.isNotBlank() || email.isNotBlank()) {
                        append(" تأكد من تضمين بيانات التواصل في التقرير.")
=======
                val reportDirectory = File(context.cacheDir, "shared_reports")
                if (!reportDirectory.exists() && !reportDirectory.mkdirs()) {
                    throw IllegalStateException("تعذر إنشاء مجلد التقرير")
                }

                val pdfFile = File(
                    reportDirectory,
                    "${safeName}_${System.currentTimeMillis()}.pdf"
                )

                FileOutputStream(pdfFile).use { output ->
                    pdf.writeTo(output)
                }

                val authority = "${context.packageName}.fileprovider"
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    authority,
                    pdfFile
                )

                val shareText = buildString {
                    append("تقرير: ")
                    append(jobName.ifBlank { "تقرير" })
                    if (phone.isNotBlank()) {
                        append("\nالهاتف: ")
                        append(phone)
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
                    }
<<<<<<< HEAD

                    append(" — ")
=======
                    if (email.isNotBlank()) {
                        append("\nالبريد الإلكتروني: ")
                        append(email)
                    }
                    append("\n")
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
                    append(NotebkBrand.APP_NAME)
                }

<<<<<<< HEAD
                Toast.makeText(
                    context,
                    details,
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "تعذر فتح طباعة PDF: ${e.message ?: "خطأ غير معروف"}",
                    Toast.LENGTH_LONG
                ).show()

                onFinished()
=======
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, jobName.ifBlank { "تقرير" })
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    clipData = android.content.ClipData.newUri(
                        context.contentResolver,
                        pdfFile.name,
                        uri
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(
                    shareIntent,
                    "مشاركة التقرير"
                ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (context !is android.app.Activity) {
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(chooser)
            } finally {
                pdf.close()
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "تعذرت مشاركة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            // Restore the WebView's original dimensions so its screen layout
            // is not left expanded after PDF generation.
            try {
                if (originalWidth > 0 && originalHeight > 0) {
                    webView.layoutParams = originalLayoutParams
                    webView.measure(
                        View.MeasureSpec.makeMeasureSpec(
                            originalWidth,
                            View.MeasureSpec.EXACTLY
                        ),
                        View.MeasureSpec.makeMeasureSpec(
                            originalHeight,
                            View.MeasureSpec.EXACTLY
                        )
                    )
                    webView.layout(
                        webView.left,
                        webView.top,
                        webView.left + originalWidth,
                        webView.top + originalHeight
                    )
                }
            } catch (_: Exception) {
                // Restoration is best-effort and must not hide the main result.
            }
            onFinished()
        }
<<<<<<< HEAD
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "تعذر تجهيز التقرير للطباعة: ${e.message ?: "خطأ غير معروف"}",
            Toast.LENGTH_LONG
        ).show()

        onFinished()
=======
>>>>>>> branch 'main' of https://github.com/alhlwqya-debug/notebk
    }
}