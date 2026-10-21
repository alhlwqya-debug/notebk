package com.ahd.notebk.data.report

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Toast
import com.ahd.notebk.ui.components.NotebkBrand

/**
 * Opens Android's native print dialog for the rendered report.
 *
 * Users can select "Save as PDF" from the system print dialog.
 * PrintManager manages the print callbacks.
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

    try {
        webView.post {
            try {
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
                }

                printManager.print(safeName, adapter, attributes)

                val details = buildString {
                    append("تم فتح نافذة طباعة التقرير.")
                    append(" اختر «حفظ بتنسيق PDF» لإنشاء الملف.")

                    if (phone.isNotBlank() || email.isNotBlank()) {
                        append(" تأكد من تضمين بيانات التواصل في التقرير.")
                    }

                    append(" — ")
                    append(NotebkBrand.APP_NAME)
                }

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
            }
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "تعذر تجهيز التقرير للطباعة: ${e.message ?: "خطأ غير معروف"}",
            Toast.LENGTH_LONG
        ).show()

        onFinished()
    }
}