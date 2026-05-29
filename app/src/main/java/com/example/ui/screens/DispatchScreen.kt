package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RouteItem
import com.example.data.model.SoItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.OdooViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchScreen(
    viewModel: OdooViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val routesList by viewModel.routes.collectAsState()
    val isRoutesLoading by viewModel.routesLoading.collectAsState()
    val routesError by viewModel.routesError.collectAsState()

    var selectedRouteName by remember { mutableStateOf<String?>(null) }
    var selectedRouteColor by remember { mutableStateOf("#333333") }

    // Table state
    var routeSos by remember { mutableStateOf<List<SoItem>>(emptyList()) }
    var isSosLoading by remember { mutableStateOf(false) }
    var sosError by remember { mutableStateOf<String?>(null) }

    val selectedSoIds = remember { mutableStateListOf<Int>() }
    // Inline text-fields for individual transport notes: orderId -> note text
    val notesMap = remember { mutableStateMapOf<Int, String>() }

    // Form confirmation State
    var showDispatchDialog by remember { mutableStateOf(false) }

    // Start polling when on details page
    LaunchedEffect(selectedRouteName) {
        val route = selectedRouteName
        if (route == null) {
            viewModel.loadRoutes()
            routeSos = emptyList()
            selectedSoIds.clear()
            notesMap.clear()
        } else {
            // Fetch route details
            isSosLoading = true
            try {
                val det = viewModel.repository.getRouteDetails(route)
                routeSos = det.sos
                // Update selected checkboxes state
                selectedSoIds.clear()
            } catch (e: Exception) {
                sosError = e.message ?: "ข้อผิดพลาดในการโหลดรายการ"
            } finally {
                isSosLoading = false
            }

            // Simulate POLLING loop for new items (every 10 seconds)
            launch {
                while (true) {
                    delay(12000)
                    viewModel.repository.simulateLiveOrderArrival()
                    try {
                        val freshDest = viewModel.repository.getRouteDetails(route)
                        val freshList = freshDest.sos
                        
                        // Check if new items arrived
                        val prevCount = routeSos.size
                        val newCount = freshList.size
                        if (newCount > prevCount) {
                            val diff = freshList.size - routeSos.size
                            Toast.makeText(context, "🔔 มีคำสั่งซื้อเข้ามาใหม่ $diff รายการ ในเส้นทางนี้!", Toast.LENGTH_LONG).show()
                        }
                        routeSos = freshList
                    } catch (e: Exception) {
                        // silent poller error
                    }
                }
            }
        }
    }

    // Helper functions
    fun toggleSoSelection(id: Int) {
        if (selectedSoIds.contains(id)) {
            selectedSoIds.remove(id)
        } else {
            selectedSoIds.add(id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = selectedRouteName ?: "🚛 ระบบขึ้นรถสินค้า ณ คลังกระจาย",
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalText,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedRouteName == null) {
                                onBack()
                            } else {
                                selectedRouteName = null
                            }
                        },
                        modifier = Modifier.testTag("dispatch_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ProfessionalText)
                    }
                },
                actions = {
                    if (selectedRouteName != null) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                isSosLoading = true
                                try {
                                    val det = viewModel.repository.getRouteDetails(selectedRouteName!!)
                                    routeSos = det.sos
                                } catch (e: Exception) {
                                    sosError = e.message
                                } finally {
                                    isSosLoading = false
                                }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh details", tint = ProfessionalText)
                        }
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ProfessionalBackground)
        ) {
            if (selectedRouteName == null) {
                // SCREEN 1: GRID FOR ROUTES SELECTION
                when {
                    isRoutesLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = ProfessionalPlum)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("กำลังดึงสายส่งทั้งหมด...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    routesError != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(routesError!!, color = Color.Red, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadRoutes() }, colors = ButtonDefaults.buttonColors(containerColor = OdooBlueLink)) {
                                Text("ลองใหม่")
                            }
                        }
                    }
                    routesList.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ไม่มีเส้นทางเดินรถค้างส่งในระบบขณะนี้", color = TextMuted, fontSize = 16.sp)
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "เลือกเส้นทางจัดส่งเพื่อดูใบตรวจสอบสินค้าขึ้นรถส่งลูกค้า (Tablet Dispatch):",
                                color = ProfessionalText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 250.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(routesList) { route ->
                                    RouteButtonCard(
                                        route = route,
                                        onClick = {
                                            selectedRouteColor = route.color
                                            selectedRouteName = route.route
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // SCREEN 2: TABLE GRID DETAILS FOR CURRENT ROUTE
                Column(modifier = Modifier.fillMaxSize()) {
                    // Summary status toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .border(width = 1.dp, color = ProfessionalOutline, shape = RoundedCornerShape(14.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val hexColor = try { Color(android.graphics.Color.parseColor(selectedRouteColor)) } catch (e: Exception) { ProfessionalPlum }
                            Text(
                                text = selectedRouteName ?: "",
                                fontWeight = FontWeight.Bold,
                                color = hexColor,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "จำนวนคำสั่งซื้อค้างส่งในระบบ: ${routeSos.size} รายการ",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { showDispatchDialog = true },
                            enabled = selectedSoIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfessionalPlum,
                                disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (selectedSoIds.isEmpty()) BorderStroke(1.dp, ProfessionalOutline) else null,
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("table_dispatch_button")
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = "Load to container", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("นำสินค้าขึ้นรถ (${selectedSoIds.size})", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    when {
                        isSosLoading && routeSos.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = OdooBlueLink)
                            }
                        }
                        routeSos.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("✓", fontSize = 48.sp, color = OdooGreen)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("ไม่มีสินค้าคงค้างในเส้นทางรถสายนี้", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("คำสั่งซื้อขึ้นรถออกจัดส่งเสร็จสิ้นสมบูรณ์", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        else -> {
                            // Render table content inside high contrast dark scroll view
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(routeSos) { soItem ->
                                    val hasNote = notesMap[soItem.soId] ?: ""
                                    DispatchTableCard(
                                        so = soItem,
                                        isSelected = selectedSoIds.contains(soItem.soId),
                                        noteText = hasNote,
                                        onNoteChange = { notesMap[soItem.soId] = it },
                                        onToggle = { toggleSoSelection(soItem.soId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MODAL VEHICLE FORM ENQUIRY
            if (showDispatchDialog && selectedRouteName != null) {
                DispatchFormDialog(
                    routeName = selectedRouteName!!,
                    selectedSos = routeSos.filter { it.soId in selectedSoIds },
                    notes = notesMap.filter { it.key in selectedSoIds }.mapKeys { it.key.toString() },
                    viewModel = viewModel,
                    onDismiss = { showDispatchDialog = false },
                    onDispatched = { docNo, pdfName ->
                        showDispatchDialog = false
                        // Clear dispatched ones
                        routeSos = routeSos.filter { it.soId !in selectedSoIds }
                        selectedSoIds.clear()
                        notesMap.clear()
                        Toast.makeText(context, "✓ ดำน์โหลดเอกสาร $pdfName สำเร็จ\nเลขเอกสารจัดขึ้นรถ: $docNo", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@Composable
fun RouteButtonCard(
    route: RouteItem,
    onClick: () -> Unit
) {
    val routeColor = try {
        Color(android.graphics.Color.parseColor(route.color))
    } catch (e: Exception) {
        ProfessionalPlum
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("route_card_${route.route}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ProfessionalOutline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Big Emoji Icon
            Text(route.icon, fontSize = 48.vibrateSp())

            Text(
                text = route.route,
                color = routeColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                color = routeColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "${route.soCount} ใบ ค้างส่งจัดขื้นรถ",
                    color = routeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// Tablet-First responsive Font sizing helper
fun Int.vibrateSp() = this.sp

@Composable
fun DispatchTableCard(
    so: SoItem,
    isSelected: Boolean,
    noteText: String,
    onNoteChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("so_table_row_${so.soId}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ProfessionalAccentContainer.copy(alpha = 0.4f) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) ProfessionalPlum else ProfessionalOutline
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onToggle() }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // First Row: Checkbox, S.O. Code, Received Bill Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = ProfessionalPlum),
                    modifier = Modifier.testTag("table_cb_${so.soId}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = so.so,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalPlum,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                // Received Bill Indicator Badge
                Surface(
                    color = if (so.received) ProfessionalAccentContainer else Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (so.received) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = "bill status icon",
                            tint = if (so.received) ProfessionalPlum else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (so.received) "รับบิลแล้ว" else "ยังไม่รับบิล",
                            color = if (so.received) ProfessionalPlum else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Customer Name & Province
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = so.customer,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfessionalText,
                    fontSize = 14.sp
                )
                Text(
                    text = "📍 จังหวัด: ${so.province}  |  🚛 ขนส่งหลัก: ${so.carrier}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Box dimensions: Package counts and Quantities
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
                    .background(ProfessionalBackground, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📦 จำนวนห่อ/แพ็คเกจ:   ${so.packages} แพ็ค",
                    color = ProfessionalPlum,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Text(
                    text = "QTY ทั้งหมด:   ${so.qty} ชิ้น",
                    color = ProfessionalText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // Note edit text inside row card
            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteChange,
                label = { Text("ขนส่งต่อ / ทะเบียนคนรถขนถ่าย (Carrier Notes)") },
                placeholder = { Text("ใส่หมายเหตุรถขนแทน...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                    .testTag("carrier_note_${so.soId}"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ProfessionalText,
                    unfocusedTextColor = ProfessionalText,
                    focusedBorderColor = ProfessionalPlum,
                    unfocusedBorderColor = ProfessionalOutline,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                maxLines = 1,
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DispatchFormDialog(
    routeName: String,
    selectedSos: List<SoItem>,
    notes: Map<String, String>,
    viewModel: OdooViewModel,
    onDismiss: () -> Unit,
    onDispatched: (String, String) -> Unit
) {
    var plateNo by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var departTime by remember { mutableStateOf("") }
    var standsLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        departTime = formatter.format(Date())
    }

    Dialog(
        onDismissRequest = { if (!standsLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("dispatch_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ProfessionalOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🚛 ข้อมูลพาหนะจัดส่งคลัง",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalText
                )

                Text(
                    text = "เส้นทาง: $routeName | นำขึ้นรถทั้งหมด ${selectedSos.size} รายการ",
                    fontSize = 13.sp,
                    color = ProfessionalPlum
                )

                // Selected S.O. Scrollable list inside Light Modal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProfessionalBackground)
                        .padding(8.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(selectedSos) { so ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ProfessionalPlum, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${so.so} - [${so.packages} pkg] ${so.customer}",
                                    fontSize = 11.sp,
                                    color = ProfessionalText
                                )
                            }
                        }
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = plateNo,
                    onValueChange = { plateNo = it },
                    label = { Text("ทะเบียนรถจัดส่ง *") },
                    placeholder = { Text("e.g. กข 1234 กรุงเทพฯ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plate_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProfessionalText, unfocusedTextColor = ProfessionalText, focusedBorderColor = ProfessionalPlum, unfocusedBorderColor = ProfessionalOutline
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("ชื่อพนักงานขับรถ *") },
                    placeholder = { Text("e.g. นายสมชาย ยอมเดช") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProfessionalText, unfocusedTextColor = ProfessionalText, focusedBorderColor = ProfessionalPlum, unfocusedBorderColor = ProfessionalOutline
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = departTime,
                    onValueChange = { departTime = it },
                    label = { Text("เวลาออกรถ *") },
                    placeholder = { Text("13:45") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("depart_time_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProfessionalText, unfocusedTextColor = ProfessionalText, focusedBorderColor = ProfessionalPlum, unfocusedBorderColor = ProfessionalOutline
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (!standsLoading) onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("ยกเลิก", color = Color.DarkGray)
                    }

                    Button(
                        onClick = {
                            standsLoading = true
                            coroutineScope.launch {
                                val res = viewModel.repository.confirmDispatch(
                                    routeName = routeName,
                                    soIds = selectedSos.map { it.soId },
                                    plate = plateNo,
                                    driver = driverName,
                                    departTime = departTime,
                                    notes = notes
                                )
                                if (res.ok) {
                                    onDispatched(
                                        res.docNo ?: "DO-SUCCESS",
                                        res.pdfName ?: "dispatch.pdf"
                                    )
                                } else {
                                    // simple show detail
                                }
                                standsLoading = false
                            }
                        },
                        enabled = plateNo.trim().isNotEmpty() && driverName.trim().isNotEmpty() && departTime.trim().isNotEmpty() && !standsLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalPlum,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("dispatch_submit_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (standsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("ยืนยันขึ้นรถออกส่ง", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
