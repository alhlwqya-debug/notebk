package com.ahd.notebk.ui.screen.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.ahd.notebk.data.report.TailorPdfReport
import com.ahd.notebk.ui.screen.ledger.LedgerViewModel
import com.ahd.notebk.ui.components.BrandTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: LedgerViewModel, reportType: String = "all", onBack: () -> Unit) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val records by viewModel.recordsState.collectAsState()
    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var file by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun moveMonth(delta: Int) {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        selectedYear = c.get(Calendar.YEAR)
        selectedMonth = c.get(Calendar.MONTH)
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        val source = file
        if (uri != null && source != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    source.inputStream().use { input -> input.copyTo(output) }
                } ?: error("تعذر فتح ملف الحفظ")
            }.onSuccess {
                Toast.makeText(context, "تم حفظ ملف PDF", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "تعذر حفظ ملف PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(records, settings, selectedYear, selectedMonth) {
        isGenerating = true
        error = null
        val sourceRecords = remember(records, reportType) {
        when (reportType) {
            "individual" -> records.filter { it.recordType == "individual" }
            "numeric" -> records.filter { it.recordType == "numeric" }
            else -> records
        }
    }

    LaunchedEffect(sourceRecords, settings, selectedYear, selectedMonth) {
        isGenerating = true
        error = null
        val result = withContext(Dispatchers.IO) {
            runCatching {
                TailorPdfReport.create(context, sourceRecords, settings, selectedYear, selectedMonth)
            }
        }
        file = result.getOrNull()
        error = result.exceptionOrNull()?.message ?: if (result.isFailure) "تعذر إنشاء التقرير" else null
        isGenerating = false
    }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale("ar", "YE")).format(
        Calendar.getInstance().apply { set(selectedYear, selectedMonth, 1) }.time
    )

    Scaffold(
        topBar = {
            BrandTopBar(title = "التقارير والطباعة", onBack = onBack)
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                when (reportType) {
                    "individual" -> "تقرير السجل الفردي"
                    "numeric" -> "تقرير السجل العددي"
                    else -> "التقرير الشهري العام"
                },
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { moveMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "الشهر السابق")
                }
                Text(monthName, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { moveMonth(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "الشهر التالي")
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("محتوى التقرير", fontWeight = FontWeight.Bold)
                    Text("الملخص المالي، إجمالي القطع، ملخص أنواع القطع، النشاط، والسجل اليومي.")
                    Text("مصدر التقرير: ${when (reportType) { "individual" -> "السجل الفردي فقط"; "numeric" -> "السجل العددي فقط"; else -> "كل السجلات" }}")
                    Text("السجلات: ${sourceRecords.count { record ->
                        val c = Calendar.getInstance().apply { timeInMillis = record.timestamp }
                        c.get(Calendar.YEAR) == selectedYear && c.get(Calendar.MONTH) == selectedMonth
                    }}")
                    if (isGenerating) Text("جاري تجهيز ملف PDF...")
                    error?.let { Text("خطأ: $it") }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = file != null && !isGenerating,
                    onClick = { file?.let { printPdf(context, it) } },
                    modifier = Modifier.weight(1f)
                ) { Text("🖨 طباعة") }
                OutlinedButton(
                    enabled = file != null && !isGenerating,
                    onClick = { file?.let { sharePdf(context, it) } },
                    modifier = Modifier.weight(1f)
                ) { Text("مشاركة") }
            }

            OutlinedButton(
                enabled = file != null && !isGenerating,
                onClick = {
                    savePdfLauncher.launch("notebk_${selectedYear}_${String.format("%02d", selectedMonth + 1)}.pdf")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("حفظ PDF في الجهاز") }
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
    printManager.print(
        "تقرير دفتر الخياط",
        PdfPrintAdapter(file),
        PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
    )
}

private class PdfPrintAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback.onLayoutFinished(info, oldAttributes != newAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        try {
            FileInputStream(file).use { input ->
                FileOutputStream(destination.fileDescriptor).use { output -> input.copyTo(output) }
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            if (cancellationSignal?.isCanceled == true) callback.onWriteCancelled()
            else callback.onWriteFailed(e.message)
        } finally {
            destination.close()
        }
    }
}
