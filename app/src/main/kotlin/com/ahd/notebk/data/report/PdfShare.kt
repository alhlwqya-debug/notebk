package com.ahd.notebk.data.report

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Toast
import com.ahd.notebk.ui.components.NotebkBrand

/**
 * Opens Android's native print dialog for the rendered report.
 *
 * PrintManager owns the PrintDocumentAdapter callbacks. App code must not
 * instantiate LayoutResultCallback/WriteResultCallback directly because their
 * constructors are package-private in Android SDK stubs.
 * Users can select "Save as PDF" in the system dialog, then share that file.
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
    val safeName = jobName.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_")
        .trim('_')
        .ifBlank { "report" }

    try {
        webView.post {
            try {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val attributes = PrintAttributes.Builder()
                    .setMediaSize(
                        if (landscape) {
                            reportMediaSize(context).let { size ->
                                if (size.isPortrait) size.asLandscape() else size
                            }
                        } else {
                            reportMediaSize(context)
                        }
                    )
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
                        cancellationSignal: android.os.CancellationSignal,
                        callback: LayoutResultCallback,
                        extras: android.os.Bundle?
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
                        pages: Array<android.print.PageRange>,
                        destination: android.os.ParcelFileDescriptor,
                        cancellationSignal: android.os.CancellationSignal,
                        callback: WriteResultCallback
                    ) {
                        baseAdapter.onWrite(pages, destination, cancellationSignal, callback)
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
                    append("تم فتح نافذة طباعة التقرير. اختر «حفظ بتنسيق PDF» لإنشاء الملف")
                    if (phone.isNotBlank() || email.isNotBlank()) {
                        append(" — بيانات التواصل متاحة في التقرير عند تضمينها")
                    }
                    append(" — ")
                    append(NotebkBrand.APP_NAME)
                }
                Toast.makeText(context, details, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "تعذر فتح طباعة PDF: \${e.message ?: "خطأ غير معروف"}",
                    Toast.LENGTH_LONG
                ).show()
                onFinished()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "تعذر تجهيز التقرير للطباعة: \${e.message ?: "خطأ غير معروف"}",
            Toast.LENGTH_LONG
        ).show()
        onFinished()
    }
}
