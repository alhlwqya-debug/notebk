package com.ahd.notebk.data.report

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ahd.notebk.ui.components.NotebkBrand
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
    val safeName = jobName.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_").trim('_').ifBlank { "report" }
    webView.post {
        var completed = false
        fun finishOnce() {
            if (!completed) {
                completed = true
                onFinished()
            }
        }
        try {
            val directory = File(context.cacheDir, "shared_reports")
            if (!directory.exists() && !directory.mkdirs()) error("تعذر إنشاء مجلد التقرير")
            val pdfFile = File(directory, safeName + "_" + System.currentTimeMillis() + ".pdf")
            val descriptor = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_READ_WRITE
            )
            val mediaSize = if (landscape) PrintAttributes.MediaSize.ISO_A4.asLandscape()
                else PrintAttributes.MediaSize.ISO_A4.asPortrait()
            val attributes = PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setResolution(PrintAttributes.Resolution("pdf", "PDF", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
            val adapter = webView.createPrintDocumentAdapter(safeName)
            val cancellation = CancellationSignal()
            adapter.onLayout(null, attributes, cancellation, object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentAdapter.DocumentInfo?, changed: Boolean) {
                    if (info == null || cancellation.isCanceled) {
                        runCatching { descriptor.close() }
                        pdfFile.delete()
                        Toast.makeText(context, "تعذر تجهيز صفحات التقرير", Toast.LENGTH_LONG).show()
                        adapter.onFinish()
                        finishOnce()
                        return
                    }
                    adapter.onWrite(arrayOf(PageRange.ALL_PAGES), descriptor, cancellation, object : PrintDocumentAdapter.WriteResultCallback() {
                        override fun onWriteFinished(pages: Array<out PageRange>) {
                            runCatching { descriptor.close() }
                            adapter.onFinish()
                            try {
                                check(pdfFile.exists() && pdfFile.length() > 0L) { "ملف PDF فارغ" }
                                val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                                val shareText = buildString {
                                    append("تقرير: ")
                                    append(jobName.ifBlank { "تقرير" })
                                    if (phone.isNotBlank()) append("\nالهاتف: " + phone)
                                    if (email.isNotBlank()) append("\nالبريد الإلكتروني: " + email)
                                    append("\n" + NotebkBrand.APP_NAME)
                                }
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, jobName.ifBlank { "تقرير" })
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    clipData = ClipData.newUri(context.contentResolver, pdfFile.name, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(send, "مشاركة التقرير").apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذرت مشاركة التقرير: " + (e.localizedMessage ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
                            } finally {
                                finishOnce()
                            }
                        }
                        override fun onWriteFailed(error: CharSequence?) {
                            runCatching { descriptor.close() }
                            adapter.onFinish()
                            pdfFile.delete()
                            Toast.makeText(context, "تعذر إنشاء PDF: " + (error ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
                            finishOnce()
                        }
                        override fun onWriteCancelled() {
                            runCatching { descriptor.close() }
                            adapter.onFinish()
                            pdfFile.delete()
                            finishOnce()
                        }
                    })
                }
                override fun onLayoutFailed(error: CharSequence?) {
                    runCatching { descriptor.close() }
                    adapter.onFinish()
                    pdfFile.delete()
                    Toast.makeText(context, "تعذر تخطيط التقرير: " + (error ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
                    finishOnce()
                }
                override fun onLayoutCancelled() {
                    runCatching { descriptor.close() }
                    adapter.onFinish()
                    pdfFile.delete()
                    finishOnce()
                }
            }, null)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذرت مشاركة التقرير: " + (e.localizedMessage ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
            finishOnce()
        }
    }
}
