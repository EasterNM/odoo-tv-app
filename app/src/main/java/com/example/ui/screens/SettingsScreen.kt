package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.theme.*
import com.example.ui.viewmodel.OdooViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: OdooViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val rawUrl by viewModel.baseUrl.collectAsState()
    val simMode by viewModel.useSimulation.collectAsState()

    var urlInput by remember { mutableStateOf(rawUrl) }
    var simModeChecked by remember { mutableStateOf(simMode) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var testColor by remember { mutableStateOf(Color.Gray) }
    var isTesting by remember { mutableStateOf(false) }

    LaunchedEffect(rawUrl, simMode) {
        urlInput = rawUrl
        simModeChecked = simMode
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("⚙️ ตั้งค่าระบบ (Settings)", fontWeight = FontWeight.Bold, color = ProfessionalText, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ProfessionalText)
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ProfessionalBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "กำหนดค่าการเชื่อมต่อ Odoo TV Dashboard Server เพื่อแปลงแอปเป็นแอปใช้งานจริงบนระบบเครือข่าย",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ProfessionalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "โหมดการทำงาน",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ใช้โหมดจำลองสถานะออฟไลน์ (Offline)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                "เปิดโหมดนี้เพื่อใช้งานแอปได้ทันทีโดยไม่ต้องต่อเซิร์ฟเวอร์จริง (เหมาะกับการทดสอบและรีวิว)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = simModeChecked,
                            onCheckedChange = {
                                simModeChecked = it
                                viewModel.updateSettings(urlInput, it)
                            },
                            modifier = Modifier.testTag("simulation_toggle")
                        )
                    }
                }
            }

            AnimatedVisibility(visible = !simModeChecked) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ProfessionalOutline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "เซิร์ฟเวอร์เชื่อมต่อ (Server Connection)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("URL เซิร์ฟเวอร์ (e.g. Host/Render.com)") },
                            placeholder = { Text("https://your-odoo-tv-dashboard.onrender.com") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = "API url") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_input_field"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                viewModel.updateSettings(urlInput, false)
                                testStatus = "กำลังทดสอบเชื่อมต่อ..."
                                testColor = Color.Gray
                                isTesting = true
                                coroutineScope.launch {
                                    val testUrl = if (urlInput.endsWith("/")) urlInput else "$urlInput/"
                                    val destination = "${testUrl}api/mobile/pending-receipts"
                                    val success = testConnectionUrl(destination)
                                    if (success) {
                                        testStatus = "✓ เชื่อมต่อสำเร็จ! ดึงข้อมูลได้จริง"
                                        testColor = Color(0xFF2E7D32)
                                    } else {
                                        testStatus = "✗ เชื่อมต่อล้มเหลว กรุณาตรวจสอบ URL หรือเครือข่ายอินเทอร์เน็ต"
                                        testColor = Color(0xFFD32F2F)
                                    }
                                    isTesting = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_and_test_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("บันทึก & ทดสอบเครือข่าย (Save & Test Connection)")
                        }

                        testStatus?.let { status ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(testColor.copy(alpha = 0.15f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = status,
                                    color = testColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = simModeChecked) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ProfessionalWarningContainer.copy(alpha = 0.7f)
                    ),
                    border = BorderStroke(1.dp, ProfessionalWarningText.copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ควบคุมเครื่องจำลองสถานะ (Simulator Status)",
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalWarningText
                        )
                        Text(
                            text = "เมื่ออยู่ในโหมดจำลอง แอปจะเพิ่ม S.O. ใหม่จำลองเข้ามาเรื่อยๆ ทุก 12 วินาทีในพื้นหลังเมื่อเปิดหน้าขึ้นรถ เพื่อให้คุณเห็นอนิเมชั่นสีแถวไฮไลท์สดกะพริบที่สะท้อนระบบ Push, Polling เสมือนจริง!",
                            fontSize = 12.sp,
                            color = ProfessionalWarningText.copy(alpha = 0.8f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.resetSimulation()
                                    testStatus = "รีเซ็ตข้อมูลจำลองเรียบร้อยแล้ว!"
                                    testColor = Color(0xFF2E7D32)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f).testTag("reset_data_button")
                            ) {
                                Text("ล้าง & รีเซ็ตข้อมูลใหม่", color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Odoo TV Mobile App v1.0", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    "แปลงโค้ดจากเว็บ EasterNM/odoo-tv-dashboard เป็นเนทีฟแอปสมบูรณ์แบบ",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

suspend fun testConnectionUrl(targetUrl: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val url = URL(targetUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 4000
        conn.readTimeout = 4000
        val code = conn.responseCode
        // Any 2xx or 3xx or even 4xx response implies connection succeeded, but let's check for 200 or 404/405/401/403 (host exists)
        return@withContext code in 200..405
    } catch (e: Exception) {
        return@withContext false
    }
}
