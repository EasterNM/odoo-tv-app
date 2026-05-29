package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.OdooViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: OdooViewModel,
    onNavigateToReceipts: () -> Unit,
    onNavigateToDispatch: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val pendingReceipts by viewModel.pendingReceipts.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val isSimulation by viewModel.useSimulation.collectAsState()

    // Calculate total dispatch orders count
    val totalDispatchOrders = routes.sumOf { it.soCount }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ProfessionalPlum),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White)
                            )
                        }
                        Text(
                            text = "Odoo TV Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalText,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFD3E3FD))
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF001D36),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.drawBehind {
                    // Thin subtle bottom border matching the mockup border-[#E1E2EC]
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Hero Banner with Professional Lavender Gradient/Card styling (#E7E0FF)
            Card(
                colors = CardDefaults.cardColors(containerColor = ProfessionalAccentContainer),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ระบบบริหารคลังสินค้าหลัก",
                            color = Color(0xFF1C1B1F),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF1C1B1F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isSimulation) "⚡ โหมดสาธิตออฟไลน์" else "🔌 เซิร์ฟเวอร์ API ออนไลน์",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F),
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (isSimulation) "ผู้ตรวจประเมินสามารถกดเซ็นเอกสารและลงความเห็นจำลองได้อย่างสมบูรณ์" else "เชื่อมต่อระบบส่งข้อมูลตรง Odoo Backend",
                        color = Color(0xFF49454F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            // Warning Banner matching mockup: bg-[#F9DEDC] with text-[#410002]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ProfessionalWarningContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = ProfessionalWarningText,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ข้อความแจ้งเตือนจากระบบ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalWarningText
                    )
                    Text(
                        text = "กรุณาลงนามเซ็นรับเอกสารหรือโหลดสินค้าให้เรียบร้อยเพื่อซิงค์ข้อมูล",
                        fontSize = 11.sp,
                        color = ProfessionalWarningText.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ProfessionalWarningText,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "เลือกกระบวนงานคลังสินค้าหลักเพื่อดําเนินการ:",
                color = ProfessionalText.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            // OPTION 1: MOBILE RECEIVE BILLS (Styled white, border C4C6CF as in mockup)
            RoleDashboardCard(
                title = "แผนกรับเอกสารบิล (Mobile Receipt)",
                description = "ตรวจรับบิลจากแผนกเซลล์ ลงนามเซ็นรับของบนอุปกรณ์สัมผัสเพื่อยืนยันคลังสินค้า",
                count = pendingReceipts.size,
                countUnit = "บิลค้างรับ",
                iconText = "🧾",
                iconColor = ProfessionalPlum,
                onClick = onNavigateToReceipts,
                testTag = "nav_receipts_card"
            )

            // OPTION 2: TABLET DISPATCH LOADER (Styled white, border C4C6CF as in mockup)
            RoleDashboardCard(
                title = "แผนกขึ้นตู้จัดรถ (Tablet Dispatch)",
                description = "ตรวจสอบสินค้าขึ้นรถขนส่งตามแต่ละสายหลัก ใส่ข้อมูลทะเบียนพาหนะ ชื่อคนขับ และเวลาปล่อยรถ",
                count = totalDispatchOrders,
                countUnit = "SO รอขึ้นตู้",
                iconText = "🚛",
                iconColor = ProfessionalBlue,
                onClick = onNavigateToDispatch,
                testTag = "nav_dispatch_card"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub info block
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ProfessionalOutline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info",
                        tint = ProfessionalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "การกำหนดค่าการเชื่อมต่อ Odoo Service",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ProfessionalText
                        )
                        Text(
                            text = "คุณสามารถตั้งค่า URL และฐานข้อมูลเซิร์ฟเวอร์จริงได้โดยคลิกปุ่มไอคอน ⚙️ มุมขวาบน และปิดโหมดสาธิต",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleDashboardCard(
    title: String,
    description: String,
    count: Int,
    countUnit: String,
    iconText: String,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp), // Precisely match design HTML layout rule (rounded-24px)
        border = BorderStroke(1.dp, ProfessionalOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon layout matching Tailwind mockup
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconText, fontSize = 26.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalText,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color(0xFF44474E),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats badge tag
                Surface(
                    color = iconColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$count $countUnit",
                        color = iconColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select option",
                tint = iconColor.copy(alpha = 0.6f)
            )
        }
    }
}
