package com.ahd.notebk.data.report

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Toast

/**
 * Uses Android's native WebView printing pipeline instead of manually drawing pages.
 * Android handles pagination and PDF rendering; no callback constructors are created here.
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
            val mediaSize = if (landscape) {
                PrintAttributes.MediaSize.ISO_A4.asLandscape()
            } else {
                PrintAttributes.MediaSize.ISO_A4
            }
            val attributes = PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            // The Android print UI can save the complete report as a PDF.
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
