package com.ahd.notebk.ui.screen.report

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.ahd.notebk.data.report.TailorPdfReport
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

@Composable
fun ReportScreen(viewModel: LedgerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val records by viewModel.recordsState.collectAsState()
    val now = remember { Calendar.getInstance() }
    var file by remember { mutableStateOf<File?>(null) }
    val year = now.get(Calendar.YEAR)
    val month = now.get(Calendar.MONTH)

    LaunchedEffect(records, settings) {
        file = TailorPdfReport.create(context, records, settings, year, month)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("التقرير الشهري") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("تقرير مالي وإداري شامل — $year - ${String.format("%02d", month + 1)}")
            Text("سيتم إنشاء التقرير بنفس بنية التقرير الشهري المعتمدة: الملخص المالي، بيانات الشهر، المؤشرات، ملخص القطع والسجل اليومي.")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = file != null, onClick = { file?.let { printPdf(context, it) } }, Modifier.weight(1f)) { Text("🖨 طباعة") }
                OutlinedButton(enabled = file != null, onClick = { file?.let { sharePdf(context, it) } }, Modifier.weight(1f)) { Text("مشاركة PDF") }
            }
            OutlinedButton(enabled = file != null, onClick = { file?.let { sharePdf(context, it) } }, Modifier.fillMaxWidth()) { Text("فتح/مشاركة ملف التقرير") }
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة تقرير دفتر الخياط"))
}

private fun printPdf(context: Context, file: File) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    printManager.print("تقرير دفتر الخياط", PdfPrintAdapter(file), PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build())
}

private class PdfPrintAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes?, cancellationSignal: CancellationSignal?, callback: LayoutResultCallback?) {
        if (cancellationSignal?.isCanceled == true) { callback?.onLayoutCancelled(); return }
        val info = PrintDocumentInfo.Builder(file.name).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(PdfDocument.PageInfo.PAGE_COUNT_UNKNOWN).build()
        callback?.onLayoutFinished(info, oldAttributes != newAttributes)
    }

    override fun onWrite(pages: Array<out android.print.PageRange>?, destination: ParcelFileDescriptor?, cancellationSignal: CancellationSignal?, callback: WriteResultCallback?) {
        if (destination == null) { callback?.onWriteFailed("لا يوجد ملف طباعة"); return }
        try {
            FileInputStream(file).use { input ->
                FileOutputStream(destination.fileDescriptor).use { output -> input.copyTo(output) }
            }
            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            if (cancellationSignal?.isCanceled == true) callback?.onWriteCancelled() else callback?.onWriteFailed(e.message)
        } finally {
            destination.close()
        }
    }
}
