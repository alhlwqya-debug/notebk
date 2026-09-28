package com.ahd.notebk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. نموذج سجل دفتر الخياطة المأخوذ من الدفتر الورقي
data class TailorDailyRecord(
    val id: Int,
    val dayName: String,     // اليوم (الجمعة، السبت، الأحد...)
    val itemQuantity: Int,   // عدد القطع المنجزة (الخانة الوسطى)
    val priceOrNote: String  // السعر / الحساب أو الملاحظة
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F6F8)
                ) {
                    TailorNotebookScreen()
                }
            }
        }
    }
}

@Composable
fun TailorNotebookScreen() {
    // تفريغ وتدوين بيانات الصفحة المعروضة في دفترك الورقي
    val dailyRecords = remember {
        mutableStateListOf(
            TailorDailyRecord(1, "الجمعة", 13, "23000"),
            TailorDailyRecord(2, "السبت", 9, "2000"),
            TailorDailyRecord(3, "الأحد", 10, "2000"),
            TailorDailyRecord(4, "الاثنين", 18, "2000"),
            TailorDailyRecord(5, "الثلاثاء", 17, "2000"),
            TailorDailyRecord(6, "الاربعاء", 7, "2000"),
            TailorDailyRecord(7, "الخميس", 19, "2500"),
            TailorDailyRecord(8, "الجمعة", 10, "2000"),
            TailorDailyRecord(9, "السبت", 12, "2300"),
            TailorDailyRecord(10, "الأحد", 15, "2000"),
            TailorDailyRecord(11, "الاثنين", 22, "2000"),
            TailorDailyRecord(12, "الثلاثاء", 17, "2000"),
            TailorDailyRecord(13, "الاربعاء", 6, "2000")
        )
    }

    // متغيرات حقول الإدخال اليومي السريع
    var selectedDay by remember { mutableStateOf("السبت") }
    var quantityInput by remember { mutableStateOf("") }
    var noteOrPriceInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        // الهيدر الرئيسي لشاشة الدفتر
        Surface(
            color = Color(0xFF1E293B),
            contentColor = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "دفتر الخياط الرقمي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "تسجيل الأيام وعدد القطع المنجزة",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                // إجمالي عدد القطع المنجزة
                val totalPieces = dailyRecords.sumOf { it.itemQuantity }
                Surface(
                    color = Color(0xFF0F766E),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("مجموع القطع", fontSize = 10.sp, color = Color.LightGray)
                        Text(
                            text = "$totalPieces قطعة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. الجدول الرقمي المسرّد لبيانات الدفتر الورقي
        Box(modifier = Modifier.weight(1f)) {
            Column {
                // ترويسة الأعمدة المطابقة للدفتر الورقي
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0))
                        .border(0.5.dp, Color(0xFF94A3B8))
                ) {
                    PaperCell(text = "اليوم", weight = 1.2f, isHeader = true)
                    PaperCell(text = "عدد القطع", weight = 1f, isHeader = true, color = Color(0xFF0369A1))
                    PaperCell(text = "الحساب / الملاحظة", weight = 1.5f, isHeader = true)
                }

                // قائمة السجلات المجدولة
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(dailyRecords) { index, record ->
                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                        ) {
                            PaperCell(text = record.dayName, weight = 1.2f, fontWeight = FontWeight.Bold)
                            PaperCell(
                                text = "${record.itemQuantity}",
                                weight = 1f,
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Bold
                            )
                            PaperCell(text = record.priceOrNote, weight = 1.5f)
                        }
                    }
                }
            }
        }

        // 3. شريط الإدخال السريع اليومي (مطابق لعملية التدوين باليد)
        Surface(
            shadowElevation = 10.dp,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "تسجيل يوم جديد في الدفتر:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // حقل اسم اليوم
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = { selectedDay = it },
                        placeholder = { Text("اليوم", fontSize = 10.sp) },
                        modifier = Modifier.weight(1.1f),
                        singleLine = true
                    )

                    // حقل عدد القطع المنجزة
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        placeholder = { Text("عدد القطع", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // حقل السعر أو الملاحظة
                    OutlinedTextField(
                        value = noteOrPriceInput,
                        onValueChange = { noteOrPriceInput = it },
                        placeholder = { Text("الحساب/ملاحظة", fontSize = 10.sp) },
                        modifier = Modifier.weight(1.3f),
                        singleLine = true
                    )

                    // زر الإضافة للدفتر
                    Button(
                        onClick = {
                            val qty = quantityInput.toIntOrNull() ?: 0
                            if (selectedDay.isNotBlank() && qty > 0) {
                                dailyRecords.add(
                                    TailorDailyRecord(
                                        id = dailyRecords.size + 1,
                                        dayName = selectedDay,
                                        itemQuantity = qty,
                                        priceOrNote = if (noteOrPriceInput.isBlank()) "-" else noteOrPriceInput
                                    )
                                )

                                // تفريغ الخانات للإدخال التالي
                                quantityInput = ""
                                noteOrPriceInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("حفظ", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// 4. خلية الجدول المسطر المخصصة
@Composable
fun RowScope.PaperCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .border(0.5.dp, Color(0xFFCBD5E1))
            .padding(vertical = 10.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 12.sp else 13.sp,
            fontWeight = if (isHeader) FontWeight.Bold else fontWeight,
            color = if (isHeader && color == Color.Unspecified) Color(0xFF1E293B) else color,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TailorNotebookPreview() {
    MaterialTheme {
        TailorNotebookScreen()
    }
}

