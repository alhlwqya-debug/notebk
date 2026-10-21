package com.ahd.notebk.data.report

import com.ahd.notebk.ui.components.NotebkBrand

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

internal fun shareWebViewAsPdf(
    context: Context,
    webView: WebView,
    jobName: String,
    phone: String,
    email: String,
    onFinished: () -> Unit,
    landscape: Boolean = false
) {
    val reportsDir = File(context.cacheDir, "shared_reports").apply { mkdirs() }
    val safeName = jobName.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_")
        .trim('_')
        .ifBlank { "report" }
    val pdfFile = File(reportsDir, System.currentTimeMillis().toString() + "_" + safeName + ".pdf")
    var finished = false

    fun finishOnce() {
        if (!finished) {
            finished = true
            onFinished()
        }
    }

    fun fail(message: String) {
        runCatching { pdfFile.delete() }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        finishOnce()
    }

    fun shareCreatedPdf() {
        try {
            if (!pdfFile.exists() || pdfFile.length() <= 0L) {
                fail("تعذر إنشاء ملف PDF: الملف فارغ")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "تقرير PDF من ${NotebkBrand.APP_NAME} — ${NotebkBrand.DEVELOPER_NAME}" +
                        if (phone.isNotBlank()) " — رقم التواصل: $phone" else ""
                )
                if (email.isNotBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("تقرير PDF", uri)
            }
            val chooser = Intent.createChooser(
                shareIntent,
                "مشاركة تقرير PDF عبر الرسائل أو واتساب أو البريد"
            ).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("تقرير PDF", uri)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "تم تجهيز تقرير PDF للمشاركة", Toast.LENGTH_SHORT).show()
            finishOnce()
        } catch (e: Exception) {
            fail("تعذر مشاركة ملف PDF: ${e.message ?: "خطأ غير معروف"}")
        }
    }

    try {
        // Use Chromium's print layout engine rather than drawing a tall WebView
        // onto manually sliced canvases. This lets the engine paginate tables,
        // repeat table headers, and calculate the actual printable page count.
        webView.post {
            try {
                val adapter = webView.createPrintDocumentAdapter(safeName)
                val cancellationSignal = CancellationSignal()
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

                adapter.onLayout(
                    null,
                    attributes,
                    cancellationSignal,
                    object : PrintDocumentAdapter.LayoutResultCallback() {
                        override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                            var descriptor: ParcelFileDescriptor? = null
                            try {
                                descriptor = ParcelFileDescriptor.open(
                                    pdfFile,
                                    ParcelFileDescriptor.MODE_CREATE or
                                        ParcelFileDescriptor.MODE_TRUNCATE or
                                        ParcelFileDescriptor.MODE_READ_WRITE
                                )
                                adapter.onWrite(
                                    arrayOf(PageRange.ALL_PAGES),
                                    descriptor,
                                    cancellationSignal,
                                    object : PrintDocumentAdapter.WriteResultCallback() {
                                        override fun onWriteFinished(pages: Array<PageRange>) {
                                            runCatching { descriptor?.close() }
                                            shareCreatedPdf()
                                        }

                                        override fun onWriteFailed(error: CharSequence?) {
                                            runCatching { descriptor?.close() }
                                            fail("تعذر إكمال صفحات PDF: ${error ?: "خطأ في الطباعة"}")
                                        }

                                        override fun onWriteCancelled() {
                                            runCatching { descriptor?.close() }
                                            fail("تم إلغاء إنشاء ملف PDF")
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                runCatching { descriptor?.close() }
                                fail("تعذر حفظ ملف PDF: ${e.message ?: "خطأ غير معروف"}")
                            }
                        }

                        override fun onLayoutFailed(error: CharSequence?) {
                            fail("تعذر تجهيز صفحات PDF: ${error ?: "خطأ في تخطيط التقرير"}")
                        }

                        override fun onLayoutCancelled() {
                            fail("تم إلغاء تجهيز صفحات PDF")
                        }
                    },
                    null
                )
            } catch (e: Exception) {
                fail("تعذر تجهيز محتوى التقرير: ${e.message ?: "خطأ غير معروف"}")
            }
        }
    } catch (e: Exception) {
        fail("تعذر إنشاء ملف PDF للمشاركة: ${e.message ?: "خطأ غير معروف"}")
    }
}
