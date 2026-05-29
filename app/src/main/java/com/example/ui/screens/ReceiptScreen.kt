package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PendingReceipt
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.drawBehind
import com.example.ui.theme.*
import com.example.ui.viewmodel.OdooViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    viewModel: OdooViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pendingList by viewModel.pendingReceipts.collectAsState()
    val isLoading by viewModel.receiptLoading.collectAsState()
    val errorMessage by viewModel.receiptError.collectAsState()

    val checkedIds = remember { mutableStateListOf<Int>() }
    var isConfirmingReceipt by remember { mutableStateOf(false) }

    // Success Screen State
    var showSuccessScreen by remember { mutableStateOf(false) }
    var successDocNo by remember { mutableStateOf("") }
    var successItems by remember { mutableStateOf<List<String>>(emptyList()) }

    // Handlers
    fun toggleItem(id: Int) {
        if (checkedIds.contains(id)) {
            checkedIds.remove(id)
        } else {
            checkedIds.add(id)
        }
    }

    fun selectAll() {
        if (checkedIds.size == pendingList.size) {
            checkedIds.clear()
        } else {
            checkedIds.clear()
            pendingList.forEach { checkedIds.add(it.id) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadReceipts()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("📋 ระบบรับบิลมือถือ", fontWeight = FontWeight.Bold, color = ProfessionalText, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("receipt_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ProfessionalText)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadReceipts() }, modifier = Modifier.testTag("receipt_refresh_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh list", tint = ProfessionalText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = ProfessionalHeaderBorder,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        bottomBar = {
            if (!showSuccessScreen && pendingList.isNotEmpty()) {
                BottomAppBar(
                    containerColor = Color.White,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectAll() },
                            modifier = Modifier
                                .weight(0.4f)
                                .height(50.dp)
                                .testTag("select_all_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ProfessionalOutline)
                        ) {
                            Text(
                                text = if (checkedIds.size == pendingList.size && pendingList.isNotEmpty()) "ยกเลิกทั้งหมด" else "เลือกทั้งหมด",
                                color = ProfessionalPlum,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { isConfirmingReceipt = true },
                            enabled = checkedIds.isNotEmpty(),
                            modifier = Modifier
                                .weight(0.6f)
                                .height(50.dp)
                                .testTag("confirm_receipt_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfessionalPlum,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "ยืนยันรับบิล (${checkedIds.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ProfessionalBackground)
        ) {
            if (showSuccessScreen) {
                // SUCCESS SCREEN
                SuccessScreenLayout(
                    docNo = successDocNo,
                    items = successItems,
                    onReload = {
                        showSuccessScreen = false
                        viewModel.loadReceipts()
                        checkedIds.clear()
                    }
                )
            } else {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = ProfessionalPlum)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("กำลังดึงข้อมูลใบสั่งซื้อค้างรับ...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Err icon", tint = Color.Red, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(errorMessage!!, color = Color.Red, textAlign = TextAlign.Center, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadReceipts() }, colors = ButtonDefaults.buttonColors(containerColor = OdooBlue)) {
                                Text("ลองใหม่อีกครั้ง")
                            }
                        }
                    }
                    pendingList.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 60.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "เสร็จสิ้น! ไม่มีรายการบิลค้างรับจากเซลล์",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = OdooGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "รายการค้างรับทั้งหมดได้รับการยืนยันและเซ็นชื่อหมดแล้ว",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadReceipts() }, colors = ButtonDefaults.buttonColors(containerColor = OdooBlue)) {
                                Text("โหลดข้อมูลใหม่ (Sync List)")
                            }
                        }
                    }
                    else -> {
                        // NORMAL BILLS LIST SCREEN
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Subheader stats info
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ProfessionalAccentContainer)
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "บิลค้างเซ็นรับของเซลล์",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(ProfessionalPlum)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${pendingList.size} รายการ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pendingList) { so ->
                                    BillRowCard(
                                        item = so,
                                        isChecked = checkedIds.contains(so.id),
                                        onToggle = { toggleItem(so.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MODAL DIALOG FOR COFIRMATION + SIGNATURE CANVAS
            if (isConfirmingReceipt) {
                SignatureAndConfirmDialog(
                    selectedItems = pendingList.filter { it.id in checkedIds },
                    viewModel = viewModel,
                    onDismiss = { isConfirmingReceipt = false },
                    onConfirmed = { docNo, lists ->
                        isConfirmingReceipt = false
                        successDocNo = docNo
                        successItems = lists
                        showSuccessScreen = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BillRowCard(
    item: PendingReceipt,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("bill_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) ProfessionalAccentContainer.copy(alpha = 0.4f) else Color.White
        ),
        border = BorderStroke(
            width = if (isChecked) 2.dp else 1.dp,
            color = if (isChecked) ProfessionalPlum else ProfessionalOutline
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .size(24.dp)
                    .testTag("checkbox_${item.id}"),
                colors = CheckboxDefaults.colors(checkedColor = ProfessionalPlum)
            )

            Column(modifier = Modifier.weight(1f)) {
                // Header with SO Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.so,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalPlum
                    )

                    item.easyNo?.let { easy ->
                        Surface(
                            color = Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "easy-acc: $easy",
                                color = Color(0xFF1D4ED8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } ?: run {
                        Text(
                            text = "ไม่มีเลข easy-acc",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Customer details
                Text(
                    text = item.customer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date stamp
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

data class StrokePath(
    val points: List<Offset> = emptyList()
)

@Composable
fun SignatureAndConfirmDialog(
    selectedItems: List<PendingReceipt>,
    viewModel: OdooViewModel,
    onDismiss: () -> Unit,
    onConfirmed: (String, List<String>) -> Unit
) {
    var signerName by remember { mutableStateOf("") }
    val strokeList = remember { mutableStateListOf<StrokePath>() }
    var hasSig by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(12.dp)
                .testTag("signature_dialog_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "✍️ เซ็นชื่อรับบิล",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OdooBlue
                )

                Text(
                    text = "ตรวจสอบรายการ บิลทั้งหมด ${selectedItems.size} ใบแล้วลงชื่อเพื่อยืนยันการรับบิล",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // List of items summarized inside dialog
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(8.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(selectedItems) { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = "check icon", tint = OdooGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${item.so} - ${item.customer}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                // Signer Name Input
                OutlinedTextField(
                    value = signerName,
                    onValueChange = { signerName = it },
                    label = { Text("ชื่อผู้รับบิล / พนักงานคลังสินค้า *") },
                    placeholder = { Text("ระบุชื่อจริง-นามสกุล") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signer_name_field"),
                    singleLine = true
                )

                // Canvas Pad Wrapper
                Text(
                    text = "ลงลายมือชื่อตรงนี้ *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(
                            width = 2.dp,
                            color = Color.Gray.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(Color(0xFFFAFAFA))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    strokeList.add(StrokePath(listOf(offset)))
                                    hasSig = true
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    if (strokeList.isNotEmpty()) {
                                        val lastIndex = strokeList.lastIndex
                                        val lastStroke = strokeList[lastIndex]
                                        strokeList[lastIndex] = lastStroke.copy(points = lastStroke.points + change.position)
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        strokeList.forEach { stroke ->
                            val path = Path()
                            if (stroke.points.isNotEmpty()) {
                                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                                for (i in 1 until stroke.points.size) {
                                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF1E1E2F),
                                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }

                    if (!hasSig) {
                        Text(
                            text = "✍️ ใช้ปากกาหรือนิ้วลากเขียนลบลายเซ็นตรงนี้",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                strokeList.clear()
                                hasSig = false
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                               .padding(4.dp)
                                .testTag("clear_signature_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Signature", tint = Color.Red)
                        }
                    }
                }

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (!isSubmitting) onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ยกเลิก", color = Color.DarkGray)
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            coroutineScope.launch {
                                // Real programmatic signature capture
                                val sigB64 = strokeListToBase64(strokeList)
                                val response = viewModel.repository.confirmBillReceipt(
                                    soIds = selectedItems.map { it.id },
                                    signerName = signerName,
                                    signatureBase64 = sigB64
                                )
                                if (response.ok) {
                                    onConfirmed(
                                        response.docNo ?: "TR-SUCCESS",
                                        selectedItems.map { "${it.so} - ${it.customer}" }
                                    )
                                } else {
                                    // Handle failure (e.g. alert)
                                }
                                isSubmitting = false
                            }
                        },
                        enabled = signerName.trim().isNotEmpty() && hasSig && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OdooGreen,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_receipt_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("ยืนยันรับบิลใบส่งของ", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Draw strokes lines inside a simulated Android graphics Bitmap to generate real PDF/API-ready PNG Base64
fun strokeListToBase64(strokes: List<StrokePath>): String {
    try {
        val bitmap = android.graphics.Bitmap.createBitmap(400, 200, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 6f
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
            isAntiAlias = true
        }
        strokes.forEach { stroke ->
            if (stroke.points.size > 1) {
                val path = android.graphics.Path()
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()
        return "data:image/png;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
        return "data:image/png;base64,TRANSPARENT_FALLBACK"
    }
}

@Composable
fun SuccessScreenLayout(
    docNo: String,
    items: List<String>,
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .testTag("success_receipt_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(38.dp))
                .background(Color(0xFFE6F4EA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = "Check", tint = OdooGreen, modifier = Modifier.size(46.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "ยืนยันรับบิลสำเร็จ!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF137333)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "ลงทะเบียนรับบิลเรียบร้อยแล้วในระบบ Odoo คลังพร้อมขึ้นรถ",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFFE8F0FE),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "เลขที่เอกสารรับบิล: $docNo",
                color = OdooBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Document itemized summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "สรุปรายการบิลที่ลงนาม (${items.size} บิล)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { line ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Check inline", tint = OdooGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = line, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onReload,
            colors = ButtonDefaults.buttonColors(containerColor = OdooBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("success_reload_button"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("กลับไปยังหน้ารายการรับบิล", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
