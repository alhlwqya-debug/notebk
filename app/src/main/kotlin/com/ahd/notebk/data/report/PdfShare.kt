package com.ahd.notebk.data.report

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Toast
import kotlin.math.roundToInt

/**
 * Uses Android's native WebView printing pipeline instead of drawing the page manually.
 * Android handles pagination and PDF rendering; no PrintDocumentAdapter callback
 * constructors are instantiated by application code.
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
        try {
            check(webView.width > 0 && webView.height > 0) {
                "صفحة التقرير غير جاهزة للطباعة"
            }

            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter = webView.createPrintDocumentAdapter(jobName.ifBlank { "report" })

            val metrics = context.resources.displayMetrics
            val pageWidthMils: Int
            val pageHeightMils: Int
            if (landscape) {
                pageWidthMils = 11690
                pageHeightMils = 8268
            } else {
                pageWidthMils = 8268
                pageHeightMils = 11690
            }

            val attributes = PrintAttributes.Builder()
                .setMediaSize(
                    if (landscape) PrintAttributes.MediaSize.ISO_A4.asLandscape()
                    else PrintAttributes.MediaSize.ISO_A4
                )
                .setResolution(PrintAttributes.Resolution("report_pdf", "Report PDF", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            // Android's print framework lays out the full document and paginates it.
            // The user can choose "Save as PDF" and then share the resulting file.
            printManager.print(jobName.ifBlank { "report" }, adapter, attributes)
            onFinished()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "تعذر فتح طباعة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}",
                Toast.LENGTH_LONG
            ).show()
            runCatching { onFinished() }
        }
    }
}
