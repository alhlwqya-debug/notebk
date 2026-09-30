package com.ahd.notebk.data.repository

import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.domain.engine.LedgerEngine
import com.ahd.notebk.domain.model.TailorRecord

/** Versioned, validated backup boundary. Keeps file-format concerns outside the UI. */
object BackupManager {
    const val CURRENT_VERSION = 7

    data class BackupData(
        val records: List<TailorRecord>,
        val settings: AppSettings
    )

    fun export(records: List<TailorRecord>, settings: AppSettings): String =
        LedgerEngine.exportBackup(records, settings, CURRENT_VERSION)

    fun import(json: String): BackupData {
        require(json.isNotBlank()) { "ملف النسخة الاحتياطية فارغ" }
        val result = LedgerEngine.importBackup(json)
        require(result.format == "notebk-ledger") { "ملف النسخة غير صالح لتطبيق Notebk" }
        require(result.version in 1..CURRENT_VERSION) { "إصدار النسخة الاحتياطية غير مدعوم" }
        require(result.records.size <= 100_000) { "النسخة الاحتياطية كبيرة جدًا" }
        result.records.forEach { record ->
            require(record.itemQuantity >= 0) { "عدد القطع غير صالح" }
            require(record.credit.isFinite() && record.debit.isFinite() && record.unitPrice.isFinite()) { "مبالغ غير صالحة" }
            require(record.credit >= 0.0 && record.debit >= 0.0 && record.unitPrice >= 0.0) { "قيمة مالية سالبة" }
            require(record.dayName.length <= 120) { "اسم اليوم غير صالح" }
            require(record.personName.length <= 160) { "اسم الشخص غير صالح" }
            require(record.pageNumber.length <= 80) { "رقم الصفحة غير صالح" }
            require(record.expenseType.length <= 160) { "نوع المصروف غير صالح" }
            require(record.recordType == "numeric" || record.recordType == "individual") { "نوع السجل غير صالح" }
            require(record.note.length <= 2000) { "الملاحظة طويلة جدًا" }
        }
        return BackupData(result.records, result.settings)
    }
}
