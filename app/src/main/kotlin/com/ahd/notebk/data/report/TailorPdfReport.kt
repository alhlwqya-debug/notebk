package com.ahd.notebk.data.report

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.ahd.notebk.R
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
    private val reportLocale = Locale("ar", "YE")

    fun create(context: Context, records: List<TailorRecord>, settings: AppSettings, year: Int, month: Int): File {
        val document = PdfDocument()
        val rows = records.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
        }.sortedBy { it.timestamp }
        val file = File(context.cacheDir, "دفتر_${year}_${month + 1}.pdf")
        var pageNumber = 0
        var page: PdfDocument.Page? = null
        lateinit var canvas: Canvas
        var y = 0f
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo)
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 255 }

        fun drawBrandHeader(pageTitle: String) {
            val logoSize = 58f
            val left = PAGE_W - MARGIN - logoSize
            val top = 12f
            canvas.drawBitmap(logo, null, RectF(left, top, left + logoSize, top + logoSize), logoPaint)
            val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.DEFAULT_BOLD
                textSize = 10f
                color = android.graphics.Color.rgb(55, 35, 110)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("notebk", left - 8f, top + 22f, brand)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.DEFAULT
                textSize = 8f
                color = android.graphics.Color.DKGRAY
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(pageTitle, left - 8f, top + 36f, sub)
            val team = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.DEFAULT_BOLD
                textSize = 7f
                color = android.graphics.Color.rgb(70, 45, 130)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("AHD DEV TEAM", left - 8f, top + 48f, team)
        }

        fun newPage() {
            page?.let { document.finishPage(it) }
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create()
            page = document.startPage(info)
            canvas = page!!.canvas
            canvas.drawColor(android.graphics.Color.WHITE)
            drawBrandHeader("دفتر الحسابات")
            y = 78f
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

        fun drawCell(x: Float, top: Float, width: Float, height: Float, text: String, paint: Paint) {
            canvas.drawLine(x, top, x + width, top, line)
            canvas.drawLine(x, top + height, x + width, top + height, line)
            canvas.drawLine(x, top, x, top + height, line)
            canvas.drawLine(x + width, top, x + width, top + height, line)
            canvas.drawText(text, x + width / 2f, top + height / 2f + paint.textSize / 3f, paint)
        }

        val todayText = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
        canvas.drawText("دفتر الحسابات", PAGE_W / 2f, y + 20, title); y += 38
        canvas.drawText(settings.ownerName.ifBlank { settings.shopName }, PAGE_W / 2f, y, head); y += 18
        canvas.drawText("تاريخ التقرير: $todayText", PAGE_W / 2f, y, body); y += 15
        if (settings.shopNumber.isNotBlank()) { canvas.drawText("رقم المحل: ${settings.shopNumber}", PAGE_W / 2f, y, body); y += 15 }
        if (settings.phone.isNotBlank()) { canvas.drawText("الهاتف: ${settings.phone}", PAGE_W / 2f, y, body); y += 15 }
        y += 8

        val totalProduction = rows.sumOf { it.credit }
        val expenses = rows.sumOf { it.debit }
        val net = totalProduction - expenses
        val pieces = rows.sumOf { it.itemQuantity }
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val days = rows.map { dateKey.format(Date(it.timestamp)) }.distinct().size
        val productionDays = rows.filter { it.credit > 0 }.map { dateKey.format(Date(it.timestamp)) }.distinct().size
        val expenseDays = rows.filter { it.debit > 0 }.map { dateKey.format(Date(it.timestamp)) }.distinct().size
        val daysInMonth = Calendar.getInstance().apply { set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
        val activity = if (daysInMonth == 0) 0 else (days * 100 / daysInMonth)

        fun section(text: String) {
            canvas.drawText(text, PAGE_W / 2f, y, head); y += 15
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line); y += 10
        }
        fun metric(label: String, value: String, x: Int) {
            val xf = x.toFloat()
            canvas.drawText(label, xf, y, boldRight)
            canvas.drawText(value, xf - 105f, y, right)
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
        canvas.drawText("#", sx[0], y, head); canvas.drawText("القطعة", sx[1], y, head); canvas.drawText("الكمية", sx[2], y, head); canvas.drawText("الإيراد", sx[3], y, head); y += 13
        pieceTypes.forEachIndexed { index, type ->
            canvas.drawText("${index + 1}", sx[0], y, body); canvas.drawText(type, sx[1], y, body); canvas.drawText(summary[index].first.toString(), sx[2], y, body); canvas.drawText("%.0f ${settings.currencySymbol}".format(summary[index].second), sx[3], y, body); y += 13
        }
        y += 7

        val widths = listOf(52f, 68f, 60f, 60f, 60f, 60f, 60f, 60f, 60f, 68f, 75f)

        fun drawTableHeader() {
            val headers = listOf("اليوم", "التاريخ") + pieceTypes + listOf("المصروف", "المجموع")
            var x = MARGIN
            headers.forEachIndexed { i, text ->
                drawCell(x, y, widths[i], 27f, text, head)
                x += widths[i]
            }
            y += 27
        }

        fun drawRow(date: Date, rowRecords: List<TailorRecord>?) {
            val dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            val dayFmt = SimpleDateFormat("EEEE", reportLocale)
            val values = pieceTypes.map { type -> rowRecords?.filter { it.pieceType == type }?.sumOf { it.itemQuantity } ?: 0 }
            val expense = rowRecords?.sumOf { it.debit } ?: 0.0
            val total = rowRecords?.sumOf { it.credit - it.debit } ?: 0.0
            val cells = listOf(dayFmt.format(date), dateFmt.format(date)) + values.map { if (it == 0) "—" else it.toString() } + listOf(if (expense == 0.0) "—" else "%.0f".format(expense), if (total == 0.0) "—" else "%.0f".format(total))
            var x = MARGIN
            cells.forEachIndexed { i, text ->
                drawCell(x, y, widths[i], 24f, text, body)
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
            val key = dateKey.format(date)
            drawRow(date, rows.filter { dateKey.format(Date(it.timestamp)) == key })
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (y > PAGE_H - 45) { newPage(); drawTableHeader() }
        val totals = pieceTypes.map { type -> rows.filter { it.pieceType == type }.sumOf { it.itemQuantity } }
        val totalCells = listOf("الإجمالي", "") + totals.map { it.toString() } + listOf("%.0f".format(expenses), "%.0f".format(net))
        var x = MARGIN
        totalCells.forEachIndexed { i, text ->
            drawCell(x, y, widths[i], 25f, text, head)
            x += widths[i]
        }
        y += 38
        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.DEFAULT
            textSize = 8f
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("notebk — $year - ${String.format("%02d", month + 1)} — $todayText", PAGE_W / 2f, minOf(y, PAGE_H - 27f), footer)
        canvas.drawText("عمل المهندس أحمد عبدالودود الدبعي", PAGE_W / 2f, minOf(y, PAGE_H - 14f), footer)
        finish()
        return file
    }
}
