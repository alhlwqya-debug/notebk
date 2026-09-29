package com.ahd.notebk.data.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.domain.model.TailorRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TailorPdfReport {
    private const val PAGE_W = 842
    private const val PAGE_H = 595
    private const val MARGIN = 28f
    private val pieceTypes = listOf("ثابت كامل", "ثابت نص", "زوج كامل", "زوج نص", "فرده كامل", "فرده نص", "فرشه")

    fun create(context: Context, records: List<TailorRecord>, settings: AppSettings, year: Int, month: Int): File {
        val document = PdfDocument()
        val rows = records.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
        }.sortedBy { it.timestamp }
        val file = File(context.cacheDir, "دفتر_${year}_${month + 1}.pdf")
        var pageNumber = 0
        var canvas: Canvas? = null
        var page: PdfDocument.Page? = null
        var y = 0f

        // Keep the drawing canvas synchronized with the current PDF page.
        // Reusing a canvas from a finished page causes a native CanvasJNI
        // null-pointer crash when the report spans multiple pages.
        lateinit var c: Canvas

        fun newPage() {
            page?.let { document.finishPage(it) }
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create()
            page = document.startPage(info)
            canvas = page!!.canvas
            c = canvas
            c.drawColor(android.graphics.Color.WHITE)
            y = MARGIN
        }

        fun finish() {
            page?.let { document.finishPage(it) }
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
        }

        newPage()
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 20f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.CENTER }
        val head = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 11f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.CENTER }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT; textSize = 9f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.CENTER }
        val right = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT; textSize = 10f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.RIGHT }
        val boldRight = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 10f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.RIGHT }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = .7f; color = android.graphics.Color.DKGRAY }

        c.drawText("دفتر الحسابات", PAGE_W / 2f, y + 20, title); y += 38
        c.drawText(settings.ownerName.ifBlank { settings.shopName }, PAGE_W / 2f, y, head); y += 18
        if (settings.shopNumber.isNotBlank()) { c.drawText("رقم المحل: ${settings.shopNumber}", PAGE_W / 2f, y, body); y += 15 }
        if (settings.phone.isNotBlank()) { c.drawText("الهاتف: ${settings.phone}", PAGE_W / 2f, y, body); y += 15 }
        y += 8

        val totalProduction = rows.sumOf { it.credit }
        val expenses = rows.sumOf { it.debit }
        val net = totalProduction - expenses
        val pieces = rows.sumOf { it.itemQuantity }
        val days = rows.map { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) }.distinct().size
        val productionDays = rows.filter { it.credit > 0 }.map { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) }.distinct().size
        val expenseDays = rows.filter { it.debit > 0 }.map { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) }.distinct().size
        val daysInMonth = Calendar.getInstance().apply { set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
        val activity = if (daysInMonth == 0) 0 else (days * 100 / daysInMonth)

        fun section(text: String) {
            c.drawText(text, PAGE_W / 2f, y, head); y += 15
            c.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line); y += 10
        }
        fun metric(label: String, value: String, x: Int) {
            val xf = x.toFloat()
            c.drawText(label, xf, y, boldRight)
            c.drawText(value, xf - 105f, y, right)
        }

        section("الملخص المالي")
        metric("إجمالي الإنتاج", "%.0f ${settings.currencySymbol}".format(totalProduction), PAGE_W - MARGIN.toInt())
        metric("المصروفات", "%.0f ${settings.currencySymbol}".format(expenses), PAGE_W - 230)
        metric("الصافي", "%.0f ${settings.currencySymbol}".format(net), PAGE_W - 405)
        metric("إجمالي القطع", pieces.toString(), PAGE_W - 580)
        y += 20

        section("بيانات الشهر")
        metric("المحل", settings.shopName, PAGE_W - MARGIN.toInt())
        metric("العامل", settings.workerName.ifBlank { settings.ownerName }, PAGE_W - 230)
        metric("تاريخ البداية", "${year}/${String.format("%02d", month + 1)}/01", PAGE_W - 405)
        metric("خصم المصروف", "نعم", PAGE_W - 580)
        y += 20

        section("مؤشرات العمل")
        metric("أيام النشاط", "$days يوم", PAGE_W - MARGIN.toInt())
        metric("أيام الإنتاج", "$productionDays يوم", PAGE_W - 230)
        metric("أيام المصروفات", "$expenseDays يوم", PAGE_W - 405)
        metric("نسبة النشاط", "$activity%", PAGE_W - 580)
        y += 17
        metric("متوسط الإنتاج", "%.0f ${settings.currencySymbol}".format(if (productionDays > 0) totalProduction / productionDays else 0.0), PAGE_W - MARGIN.toInt())
        metric("متوسط المصروف", "%.0f ${settings.currencySymbol}".format(if (expenseDays > 0) expenses / expenseDays else 0.0), PAGE_W - 230)
        y += 20

        section("ملخص القطع")
        val summary = pieceTypes.map { type -> rows.filter { it.pieceType == type }.let { typeRows -> typeRows.sumOf { it.itemQuantity } to typeRows.sumOf { it.credit } } }
        val sx = floatArrayOf(90f, 300f, 510f, 720f)
        c.drawText("#", sx[0], y, head); c.drawText("القطعة", sx[1], y, head); c.drawText("الكمية", sx[2], y, head); c.drawText("الإيراد", sx[3], y, head); y += 13
        pieceTypes.forEachIndexed { index, type ->
            c.drawText("${index + 1}", sx[0], y, body); c.drawText(type, sx[1], y, body); c.drawText(summary[index].first.toString(), sx[2], y, body); c.drawText("%.0f ${settings.currencySymbol}".format(summary[index].second), sx[3], y, body); y += 13
        }
        y += 7

        fun drawTableHeader() {
            val headers = listOf("اليوم", "التاريخ") + pieceTypes + listOf("المصروف", "المجموع")
            val widths = listOf(52f, 68f, 60f, 60f, 60f, 60f, 60f, 60f, 60f, 68f, 75f)
            var x = MARGIN
            headers.forEachIndexed { i, text ->
                c.drawRect(x, y, x + widths[i], y + 27, line)
                c.drawText(text, x + widths[i] / 2, y + 17, head)
                x += widths[i]
            }
            y += 27
        }
        fun drawRow(date: Date, rowRecords: List<TailorRecord>?) {
            val dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            val dayFmt = SimpleDateFormat("EEEE", Locale("ar"))
            val values = pieceTypes.map { type -> rowRecords?.filter { it.pieceType == type }?.sumOf { it.itemQuantity } ?: 0 }
            val expense = rowRecords?.sumOf { it.debit } ?: 0.0
            val total = rowRecords?.sumOf { it.credit - it.debit } ?: 0.0
            val cells = listOf(dayFmt.format(date), dateFmt.format(date)) + values.map { if (it == 0) "—" else it.toString() } + listOf(if (expense == 0.0) "—" else "%.0f".format(expense), if (total == 0.0) "—" else "%.0f".format(total))
            val widths = listOf(52f, 68f, 60f, 60f, 60f, 60f, 60f, 60f, 60f, 68f, 75f)
            var x = MARGIN
            cells.forEachIndexed { i, text ->
                c.drawRect(x, y, x + widths[i], y + 24, line)
                c.drawText(text, x + widths[i] / 2, y + 15, body)
                x += widths[i]
            }
            y += 24
        }

        section("السجل اليومي")
        drawTableHeader()
        val cal = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        repeat(daysInMonth) {
            if (y > PAGE_H - 55) { newPage(); drawTableHeader() }
            val date = cal.time
            val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
            drawRow(date, rows.filter { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) == key })
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (y > PAGE_H - 45) { newPage(); drawTableHeader() }
        val totals = pieceTypes.map { type -> rows.filter { it.pieceType == type }.sumOf { it.itemQuantity } }
        val totalCells = listOf("الإجمالي", "") + totals.map { it.toString() } + listOf("%.0f".format(expenses), "%.0f".format(net))
        val widths = listOf(52f, 68f, 60f, 60f, 60f, 60f, 60f, 60f, 60f, 68f, 75f)
        var x = MARGIN
        totalCells.forEachIndexed { i, text ->
            c.drawRect(x, y, x + widths[i], y + 25, line)
            c.drawText(text, x + widths[i] / 2, y + 16, boldRight.apply { textAlign = Paint.Align.CENTER })
            x += widths[i]
        }
        y += 38
        c.drawText("تقرير مالي وإداري شامل — ${year} - ${String.format("%02d", month + 1)}", PAGE_W / 2f, minOf(y, PAGE_H - 15f), body)
        finish()
        return file
    }
}
