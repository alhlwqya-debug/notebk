package com.ahd.notebk.domain.engine

import com.ahd.notebk.data.local.AppSettings
import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.domain.model.TailorRecord
import org.json.JSONArray
import org.json.JSONObject

object LedgerEngine {
    fun localDateKey(timestamp: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        return "%04d-%02d-%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    fun recalculateBalances(records: List<TailorRecord>): List<TailorRecord> {
        var balance = 0.0
        return records.sortedWith(compareBy<TailorRecord> { it.timestamp }.thenBy { it.id }).map { record ->
            balance += record.credit - record.debit
            record.copy(balance = balance)
        }
    }

    fun normalizePersonName(name: String): String =
        name.trim().replace(Regex("\\s+"), " ")

    fun personNames(records: List<TailorRecord>): List<String> =
        records.map { normalizePersonName(it.personName) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    fun recordsForPerson(records: List<TailorRecord>, personName: String?): List<TailorRecord> {
        if (personName == null) return records
        if (personName == "__UNASSIGNED__") return records.filter { normalizePersonName(it.personName).isBlank() }
        val normalized = normalizePersonName(personName)
        return records.filter { normalizePersonName(it.personName) == normalized }
    }

    fun calculateSummary(records: List<TailorRecord>): LedgerSummary {
        if (records.isEmpty()) return LedgerSummary()
        val totalPieces = records.sumOf { it.itemQuantity }
        val totalCredit = records.sumOf { it.credit }
        val totalDebit = records.sumOf { it.debit }
        val distinctDays = records.map { localDateKey(it.timestamp) }.distinct().size
        val average = if (distinctDays > 0) totalPieces.toDouble() / distinctDays else 0.0
        return LedgerSummary(totalPieces, totalCredit, totalDebit, totalCredit - totalDebit, average)
    }

    data class ImportedBackup(
        val format: String,
        val version: Int,
        val records: List<TailorRecord>,
        val settings: AppSettings
    )

    fun exportToJson(records: List<TailorRecord>): String =
        exportBackup(records, AppSettings(), 4)

    fun exportBackup(records: List<TailorRecord>, settings: AppSettings, version: Int): String {
        val root = JSONObject().apply {
            put("format", "notebk-ledger")
            put("version", version)
            put("createdAt", System.currentTimeMillis())
            put("recordCount", records.size)
            put("settings", JSONObject().apply {
                put("shopName", settings.shopName)
                put("ownerName", settings.ownerName)
                put("shopNumber", settings.shopNumber)
                put("phone", settings.phone)
                put("address", settings.address)
                put("workerName", settings.workerName)
                put("defaultPiecePrice", settings.defaultPiecePrice)
                put("currencySymbol", settings.currencySymbol)
                put("showDebitCredit", settings.showDebitCredit)
                put("autoFillToday", settings.autoFillToday)
                put("darkMode", settings.darkMode)
            })
            put("records", JSONArray().apply {
                records.forEach { item ->
                    put(JSONObject().apply {
                        put("id", item.id)
                        put("dayName", item.dayName)
                        put("itemQuantity", item.itemQuantity)
                        put("pieceType", item.pieceType)
                        put("unitPrice", item.unitPrice)
                        put("credit", item.credit)
                        put("debit", item.debit)
                        put("balance", item.balance)
                        put("note", item.note)
                        put("personName", item.personName)
                        put("pageNumber", item.pageNumber)
                        put("expenseType", item.expenseType)
                        put("recordType", item.recordType)
                        put("timestamp", item.timestamp)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun importBackup(jsonString: String): ImportedBackup {
        val root = JSONObject(jsonString)
        val format = root.optString("format", "legacy")
        val version = root.optInt("version", 1)
        val array = root.optJSONArray("records") ?: JSONArray(jsonString)
        val settingsObject = root.optJSONObject("settings")
        val settings = AppSettings(
            shopName = settingsObject?.optString("shopName", "ورشة الخياطة الرقمية") ?: "ورشة الخياطة الرقمية",
            ownerName = settingsObject?.optString("ownerName", "") ?: "",
            shopNumber = settingsObject?.optString("shopNumber", "") ?: "",
            phone = settingsObject?.optString("phone", "") ?: "",
            address = settingsObject?.optString("address", "") ?: "",
            workerName = settingsObject?.optString("workerName", "") ?: "",
            defaultPiecePrice = settingsObject?.optDouble("defaultPiecePrice", 2000.0) ?: 2000.0,
            currencySymbol = settingsObject?.optString("currencySymbol", "ر.ي") ?: "ر.ي",
            showDebitCredit = settingsObject?.optBoolean("showDebitCredit", true) ?: true,
            autoFillToday = settingsObject?.optBoolean("autoFillToday", true) ?: true,
            darkMode = settingsObject?.optBoolean("darkMode", true) ?: true
        )
        val records = buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val quantity = obj.optInt("itemQuantity", 0)
                val credit = obj.optDouble("credit", 0.0)
                val debit = obj.optDouble("debit", 0.0)
                val fallbackPrice = if (quantity > 0) {
                    val total = if (credit > 0) credit else debit
                    total / quantity
                } else 0.0
                add(TailorRecord(
                    id = obj.optInt("id", 0),
                    dayName = obj.optString("dayName", ""),
                    itemQuantity = quantity,
                    pieceType = obj.optString("pieceType", "ثابت كامل"),
                    unitPrice = obj.optDouble("unitPrice", fallbackPrice),
                    credit = credit,
                    debit = debit,
                    balance = obj.optDouble("balance", 0.0),
                    note = obj.optString("note", ""),
                    personName = normalizePersonName(obj.optString("personName", "")),
                    pageNumber = obj.optString("pageNumber", ""),
                    expenseType = obj.optString("expenseType", ""),
                    recordType = obj.optString("recordType", "numeric").ifBlank { "numeric" },
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                ))
            }
        }
        return ImportedBackup(format, version, records, settings)
    }

    fun importFromJson(jsonString: String): List<TailorRecord> = importBackup(jsonString).records
}
