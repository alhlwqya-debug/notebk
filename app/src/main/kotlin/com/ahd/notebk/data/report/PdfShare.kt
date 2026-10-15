package com.ahd.notebk.data.report

import com.ahd.notebk.ui.components.NotebkBrand

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.roundToInt

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
    val pdfFile = File(
        reportsDir,
        System.currentTimeMillis().toString() + "_" + safeName + ".pdf"
    )

    fun fail(message: String) {
        try {
            pdfFile.delete()
        } catch (_: Exception) {
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        onFinished()
    }

    /*
     * CodeAssist's Android SDK does not allow direct construction of
     * PrintDocumentAdapter.LayoutResultCallback / WriteResultCallback.
     * Keep the PDF implementation based on PdfDocument, but obtain the
     * real DOM height from WebView before measuring it. This prevents a
     * long monthly report from being limited to the currently visible
     * portion of the WebView.
     */
    fun generateAndShare(contentHeight: Int) {
        try {
            val metrics = context.resources.displayMetrics
            val viewWidth = metrics.widthPixels.coerceAtLeast(1)
            // DOM scrollHeight is measured in CSS pixels; convert it to the
            // WebView's physical-pixel coordinate space before sizing the view.
            val domHeightPx = (contentHeight * webView.scale).roundToInt()
            val measuredHeight = maxOf(
                domHeightPx,
                (webView.contentHeight * webView.scale).roundToInt(),
                webView.height,
                metrics.heightPixels
            ).coerceAtLeast(1)

            val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                viewWidth,
                android.view.View.MeasureSpec.EXACTLY
            )
            val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                measuredHeight,
                android.view.View.MeasureSpec.EXACTLY
            )

            webView.measure(widthSpec, heightSpec)
            webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)

            if (webView.measuredWidth <= 0 || webView.measuredHeight <= 0) {
                fail("تعذر قياس محتوى التقرير قبل إنشاء PDF")
                return
            }

            val originalAlpha = webView.alpha
            webView.alpha = 1f

            try {
                val document = PdfDocument()
                try {
                    // A4 dimensions in PDF points. Match the report's layout so
                    // wide individual-ledger tables are not squeezed into portrait pages.
                    val pageWidth = if (landscape) 842 else 595
                    val pageHeight = if (landscape) 595 else 842

                    val scale = pageWidth.toFloat() / webView.measuredWidth.toFloat()
                    val contentHeightPerPage = pageHeight.toFloat() / scale
                    val pageCount = ceil(
                        webView.measuredHeight.toFloat() / contentHeightPerPage
                    ).toInt().coerceAtLeast(1)

                    for (pageIndex in 0 until pageCount) {
                        val page = document.startPage(
                            PdfDocument.PageInfo.Builder(
                                pageWidth,
                                pageHeight,
                                pageIndex + 1
                            ).create()
                        )

                        page.canvas.save()
                        page.canvas.scale(scale, scale)
                        page.canvas.translate(
                            0f,
                            -pageIndex * contentHeightPerPage
                        )
                        webView.draw(page.canvas)
                        page.canvas.restore()
                        document.finishPage(page)
                    }

                    FileOutputStream(pdfFile).use { output ->
                        document.writeTo(output)
                    }
                } finally {
                    document.close()
                }
            } finally {
                webView.alpha = originalAlpha
            }

            if (!pdfFile.exists() || pdfFile.length() <= 0L) {
                fail("تم إنشاء التقرير لكن ملف PDF فارغ")
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
                if (email.isNotBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                }
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
            Toast.makeText(
                context,
                "تم تجهيز تقرير PDF كامل للمشاركة",
                Toast.LENGTH_SHORT
            ).show()
            onFinished()
        } catch (e: Exception) {
            fail("تعذر إنشاء ملف PDF للمشاركة: " + (e.message ?: "خطأ غير معروف"))
        }
    }

    try {
        // JavaScript must be enabled for evaluateJavascript to return the
        // document's full scrollHeight. These reports are generated locally
        // from escaped app data; no remote page is loaded.
        webView.post {
            webView.evaluateJavascript(
                """
                (function() {
                    Math.max(
                        document.body ? document.body.scrollHeight : 0,
                        document.documentElement ? document.documentElement.scrollHeight : 0,
                        document.body ? document.body.offsetHeight : 0,
                        document.documentElement ? document.documentElement.offsetHeight : 0
                    );
                })();
                """.trimIndent()
            ) { result ->
                val domHeight = result
                    ?.trim()
                    ?.removeSurrounding("\"")
                    ?.toFloatOrNull()
                    ?.toInt()
                    ?: 0

                generateAndShare(domHeight)
            }
        }
    } catch (e: Exception) {
        fail("تعذر تجهيز محتوى التقرير: " + (e.message ?: "خطأ غير معروف"))
    }
}
