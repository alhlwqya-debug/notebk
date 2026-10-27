package com.ahd.notebk.data.report

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ahd.notebk.ui.components.NotebkBrand
import java.io.File

/**
 * Exports the complete rendered report through Android's WebView print pipeline.
 * The framework print adapter performs pagination; never snapshot/crop the visible WebView.
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

        try {
            check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                "إنشاء PDF يتطلب Android 4.4 أو أحدث"
            }
            check(webView.width > 0 && webView.height > 0) {
                "صفحة التقرير غير جاهزة للإنشاء"
            }

            val directory = File(context.cacheDir, "shared_reports")
            if (!directory.exists()) check(directory.mkdirs()) {
                "تعذر إنشاء مجلد التقرير"
            }
            val pdfFile = File(directory, "${safeName}_${System.currentTimeMillis()}.pdf")
            val documentName = jobName.ifBlank { "report" }

            val adapter: PrintDocumentAdapter =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.createPrintDocumentAdapter(documentName)
                } else {
                    @Suppress("DEPRECATION")
                    webView.createPrintDocumentAdapter()
                }

            val mediaSize = if (landscape) {
                PrintAttributes.MediaSize.ISO_A4.asLandscape()
            } else {
                PrintAttributes.MediaSize.ISO_A4
            }
            val attributes = PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setResolution(PrintAttributes.Resolution("notebk_pdf", "Notebk PDF", 300, 300))
                .setMinMargins(PrintAttributes.Margins(250, 250, 250, 250))
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()

            val cancellation = CancellationSignal()
            adapter.onLayout(
                null,
                attributes,
                cancellation,
                object : PrintDocumentAdapter.LayoutResultCallback() {
                    override fun onLayoutFinished(
                        info: PrintDocumentInfo?,
                        changed: Boolean
                    ) {
                        if (info == null || info.pageCount == 0) {
                            pdfFile.delete()
                            fail("لم يعثر نظام الطباعة على صفحات في التقرير")
                            return
                        }

                        val descriptor = try {
                            ParcelFileDescriptor.open(
                                pdfFile,
                                ParcelFileDescriptor.MODE_CREATE or
                                    ParcelFileDescriptor.MODE_TRUNCATE or
                                    ParcelFileDescriptor.MODE_READ_WRITE
                            )
                        } catch (e: Exception) {
                            pdfFile.delete()
                            fail("تعذر إنشاء ملف PDF: ${e.localizedMessage ?: "خطأ في الملف"}")
                            return
                        }

                        try {
                            adapter.onWrite(
                                arrayOf(PageRange.ALL_PAGES),
                                descriptor,
                                cancellation,
                                object : PrintDocumentAdapter.WriteResultCallback() {
                                    override fun onWriteFinished(pages: Array<PageRange>) {
                                        runCatching { descriptor.close() }
                                        if (pages.isEmpty() || !pdfFile.isFile || pdfFile.length() <= 0L) {
                                            pdfFile.delete()
                                            fail("فشل إنشاء صفحات التقرير في ملف PDF")
                                            return
                                        }

                                        try {
                                            val uri: Uri = FileProvider.getUriForFile(
                                                context,
                                                context.packageName + ".fileprovider",
                                                pdfFile
                                            )
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
                                                clipData = ClipData.newUri(
                                                    context.contentResolver,
                                                    pdfFile.name,
                                                    uri
                                                )
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            val chooser = Intent.createChooser(
                                                sendIntent,
                                                "مشاركة التقرير"
                                            ).apply {
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                if (context !is Activity) {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            }
                                            context.startActivity(chooser)
                                        } catch (e: Exception) {
                                            pdfFile.delete()
                                            fail("تعذرت مشاركة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}")
                                            return
                                        }
                                        finishOnce()
                                    }

                                    override fun onWriteFailed(error: CharSequence?) {
                                        runCatching { descriptor.close() }
                                        pdfFile.delete()
                                        fail("فشلت طباعة التقرير: ${error ?: "خطأ غير معروف"}")
                                    }

                                    override fun onWriteCancelled() {
                                        runCatching { descriptor.close() }
                                        pdfFile.delete()
                                        finishOnce()
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            runCatching { descriptor.close() }
                            pdfFile.delete()
                            fail("تعذرت كتابة PDF: ${e.localizedMessage ?: "خطأ غير معروف"}")
                        }
                    }

                    override fun onLayoutFailed(error: CharSequence?) {
                        pdfFile.delete()
                        fail("تعذر تجهيز التقرير للطباعة: ${error ?: "خطأ غير معروف"}")
                    }

                    override fun onLayoutCancelled() {
                        pdfFile.delete()
                        finishOnce()
                    }
                },
                null
            )
        } catch (e: Exception) {
            fail("تعذرت مشاركة التقرير: ${e.localizedMessage ?: "خطأ غير معروف"}")
        }
    }
}
