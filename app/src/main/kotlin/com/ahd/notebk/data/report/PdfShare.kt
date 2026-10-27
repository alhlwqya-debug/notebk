package com.ahd.notebk.data.report

import android.content.Context
import android.webkit.WebView

/**
 * Share entry point used by the report generators.
 * The actual PDF creation and sharing implementation lives in PdfUtils.kt
 * beside the main report/print engine, so rendering logic has one owner.
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
    createAndShareRenderedPdf(
        context = context,
        webView = webView,
        jobName = jobName,
        phone = phone,
        email = email,
        onFinished = onFinished,
        landscape = landscape
    )
}
