package com.ahd.notebk.ui.screen.ledger.work

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier\nimport androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.*
import com.ahd.notebk.ui.screen.ledger.work.MonthsPage
import com.ahd.notebk.ui.screen.ledger.work.PiecesPage
import com.ahd.notebk.ui.screen.ledger.work.ReportsPage
import com.ahd.notebk.ui.screen.ledger.work.StatisticsPage

@Composable
fun WorkLedgerManagementHost(
    context: Context,
    database: LedgerDatabase,
    months: List<Month>,
    pieces: List<Piece>,
    currentBundle: MonthBundle?,
    selectedMonthId: Long?,
    showMonthsPage: Boolean,
    showPiecesPage: Boolean,
    showReportsPage: Boolean,
    showStatisticsPage: Boolean,
    editPieceTarget: Piece?,
    editPieceName: String,
    editPiecePrice: String,
    onMonthsPageChange: (Boolean) -> Unit,
    onPiecesPageChange: (Boolean) -> Unit,
    onReportsPageChange: (Boolean) -> Unit,
    onStatisticsPageChange: (Boolean) -> Unit,
    onSelectedMonthChange: (Long?) -> Unit,
    onBundleChange: (MonthBundle?) -> Unit,
    onDashboardChange: (Boolean) -> Unit,
    onNewMonth: () -> Unit,
    onRenameMonth: (Month) -> Unit,
    onCopyMonth: (Month) -> Unit,
    onDeleteMonth: (Month) -> Unit,
    onNewPiece: () -> Unit,
    onEditPiece: (Piece) -> Unit,
    onDeletePiece: (Piece) -> Unit,
    onRefresh: () -> Unit,
    onReloadBundle: () -> Unit,
    onEditPieceTargetChange: (Piece?) -> Unit,
    onEditPieceNameChange: (String) -> Unit,
    onEditPiecePriceChange: (String) -> Unit,
    onOpenReport: () -> Unit
) {
    if (showMonthsPage) {
        MonthsPage(
            months = months,
            selectedMonthId = selectedMonthId,
            onBack = { onMonthsPageChange(false) },
            onSelect = { id ->
                onSelectedMonthChange(id)
                onBundleChange(database.loadMonthBundle(id))
                onMonthsPageChange(false)
                onDashboardChange(false)
            },
            onAdd = onNewMonth,
            onRename = onRenameMonth,
            onCopy = onCopyMonth,
            onDelete = onDeleteMonth,
            onRefresh = onRefresh
        )
    }

    if (showPiecesPage) {
        PiecesPage(
            pieces = pieces,
            onBack = { onPiecesPageChange(false) },
            onAdd = onNewPiece,
            onEdit = onEditPiece,
            onDelete = onDeletePiece,
            onRefresh = onRefresh
        )
    }

    if (showReportsPage) {
        ReportsPage(
            hasMonth = currentBundle != null,
            onBack = { onReportsPageChange(false) },
            onOpenReport = { if (currentBundle != null) onOpenReport() },
            onRefresh = onRefresh
        )
    }

    if (showStatisticsPage) {
        StatisticsPage(
            bundle = currentBundle,
            database = database,
            onBack = { onStatisticsPageChange(false) },
            onRefresh = onReloadBundle
        )
    }

    if (editPieceTarget != null) {
        AlertDialog(
            onDismissRequest = { onEditPieceTargetChange(null) },
            title = { Text("تعديل القطعة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editPieceName,
                        onValueChange = onEditPieceNameChange,
                        singleLine = true,
                        label = { Text("اسم القطعة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPiecePrice,
                        onValueChange = { onEditPiecePriceChange(it.filter(Char::isDigit)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("السعر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val target = editPieceTarget
                    val name = editPieceName.trim()
                    val price = editPiecePrice.toIntOrNull()
                    if (target != null && name.isNotEmpty() && price != null && price >= 0) {
                        database.updatePiece(target.id, name, price)
                        onEditPieceTargetChange(null)
                        onReloadBundle()
                        Toast.makeText(context, "تم تحديث القطعة", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "تحقق من الاسم والسعر", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { onEditPieceTargetChange(null) }) { Text("إلغاء") } }
        )
    }
}
