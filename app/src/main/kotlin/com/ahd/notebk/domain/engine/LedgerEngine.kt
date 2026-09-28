package com.ahd.notebk.domain.engine

import com.ahd.notebk.domain.model.LedgerSummary
import com.ahd.notebk.domain.model.TailorRecord
import org.json.JSONArray
import org.json.JSONObject

object LedgerEngine {
    fun recalculateBalances(records: List<TailorRecord>): List<TailorRecord> {
        var balance = 0.0
        return records.sortedWith(compareBy<TailorRecord> { it.timestamp }.thenBy { it.id }).map { record ->
            balance += record.credit - record.debit
            record.copy(balance = balance)
        }
    }

    fun calculateSummary(records: List<TailorRecord>): LedgerSummary {
        if (records.isEmpty()) return LedgerSummary()
        val totalPieces = records.sumOf { it.itemQuantity }
        val totalCredit = records.sumOf { it.credit }
        val totalDebit = records.sumOf { it.debit }
        val distinctDays = records.map { it.dayName.trim() }.filter { it.isNotEmpty() }.distinct().size
        val average = if (distinctDays > 0) totalPieces.toDouble() / distinctDays else 0.0
        return LedgerSummary(totalPieces, totalCredit, totalDebit, totalCredit - totalDebit, average)
    }

    fun exportToJson(records: List<TailorRecord>): String {
        val root = JSONObject().apply {
            put("format", "notebk-ledger")
            put("version", 2)
            put("records", JSONArray().apply {
                records.forEach { item ->
                    put(JSONObject().apply {
                        put("id", item.id)
                        put("dayName", item.dayName)
                        put("itemQuantity", item.itemQuantity)
                        put("pieceType", item.pieceType)
                        put("credit", item.credit)
                        put("debit", item.debit)
                        put("balance", item.balance)
                        put("note", item.note)
                        put("timestamp", item.timestamp)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun importFromJson(jsonString: String): List<TailorRecord> {
        val root = JSONObject(jsonString)
        val array = root.optJSONArray("records") ?: JSONArray(jsonString)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                add(TailorRecord(
                    id = obj.optInt("id", 0),
                    dayName = obj.optString("dayName", ""),
                    itemQuantity = obj.optInt("itemQuantity", 0),
                    pieceType = obj.optString("pieceType", "ثابت كامل"),
                    credit = obj.optDouble("credit", 0.0),
                    debit = obj.optDouble("debit", 0.0),
                    balance = obj.optDouble("balance", 0.0),
                    note = obj.optString("note", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                ))
            }
        }
    }
}
