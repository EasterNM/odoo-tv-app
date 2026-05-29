package com.example.data.repository

import android.util.Log
import com.example.data.api.OdooApi
import com.example.data.local.LocalSettings
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class OdooRepository(private val settings: LocalSettings) {

    private var currentRetrofitUrl: String? = null
    private var cachedApi: OdooApi? = null

    // In-memory simulation state
    private var simPendingReceipts = mutableListOf<PendingReceipt>()
    private val simRouteSosMap = mutableMapOf<String, MutableList<SoItem>>()
    private val simRoutes = mutableListOf<RouteItem>()
    private var simReceiptCounter = 101

    init {
        resetSimulationData()
    }

    private fun getApi(): OdooApi {
        val url = settings.baseUrl.ifEmpty { "https://odoo-tv-dashboard.onrender.com" }
        val formattedUrl = if (url.endsWith("/")) url else "$url/"

        if (cachedApi != null && currentRetrofitUrl == formattedUrl) {
            return cachedApi!!
        }

        Log.d("OdooRepository", "Creating Retrofit for URL: $formattedUrl")
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val api = retrofit.create(OdooApi::class.java)
        cachedApi = api
        currentRetrofitUrl = formattedUrl
        return api
    }

    // RESET OR GENERATE RANDOM SIMULATED DATA
    fun resetSimulationData() {
        synchronized(this@OdooRepository) {
            simPendingReceipts = mutableListOf(
                PendingReceipt(12, "SO2026/05/29-01", "EZ-9081", "หจก. ศรีสมพร ออฟฟิศเสิร์ซ (สมุทรปราการ)", "2026-05-29"),
                PendingReceipt(13, "SO2026/05/29-02", "EZ-3382", "บจก. สยามบรรจุภัณฑ์ (สาขา 2)", "2026-05-29"),
                PendingReceipt(14, "SO2026/05/29-03", "EZ-7214", "ทวีผล ค้าวัสดุก่อสร้าง (พิจิตร)", "2026-05-29"),
                PendingReceipt(15, "SO2026/05/29-04", null, "เชียงใหม่ เทรดดิ้ง แอนด์ คูลเลอร์", "2026-05-29"),
                PendingReceipt(16, "SO2026/05/29-05", "EZ-1102", "บจก. ยูเนี่ยน เคมีคอล อุตสาหกรรม", "2026-05-29")
            )

            simRoutes.clear()
            simRoutes.addAll(
                listOf(
                    RouteItem("สายเหนือ (Northern Route)", "🚛", "#ef4444", 3),
                    RouteItem("สายเอเซีย (Central Route)", "🚚", "#3b82f6", 4),
                    RouteItem("สายใต้ (Southern Route)", "🚐", "#f59e0b", 2),
                    RouteItem("กรุงเทพฯ-ปริมณฑล (BKK Metropolitans)", "🛵", "#10b981", 5)
                )
            )

            simRouteSosMap.clear()
            simRouteSosMap["สายเหนือ (Northern Route)"] = mutableListOf(
                SoItem(1001, "SO2026/05/29-04", "เชียงใหม่ เทรดดิ้ง แอนด์ คูลเลอร์", "เชียงใหม่", "พาวเวอร์ขนส่ง", false, 4, 180.0),
                SoItem(1002, "SO2026/05/29-08", "กิตติพงษ์ ซุปเปอร์สโตร์", "นครสวรรค์", "นิ่มซี่เส็ง", true, 2, 75.0),
                SoItem(1003, "SO2026/05/29-09", "ทวีผล ค้าวัสดุก่อสร้าง (พิจิตร)", "พิจิตร", "Kerry Express", true, 6, 320.0)
            )

            simRouteSosMap["สายเอเซีย (Central Route)"] = mutableListOf(
                SoItem(2001, "SO2026/05/29-03", "สมมาตร โลหะกิจ", "อยุธยา", "แฟลชเอ็กซ์เพรส", true, 1, 50.0),
                SoItem(2002, "SO2026/05/29-06", "นครหลวงการค้า 1999", "นครสวรรค์", "พาวเวอร์ขนส่ง", false, 8, 410.0),
                SoItem(2003, "SO2026/05/29-11", "วิรัช ซุปเปอร์ชีป", "สิงห์บุรี", "นิ่มซี่เส็ง", true, 3, 110.0),
                SoItem(2004, "SO2026/05/29-12", "บจก. เอส.พี.เฟอร์นิเจอร์", "ชัยนาท", "หจก.ด่วนพิเศษ", false, 2, 80.0)
            )

            simRouteSosMap["สายใต้ (Southern Route)"] = mutableListOf(
                SoItem(3001, "SO2026/05/29-07", "ใต้เจริญการค้าสุราษฎร์", "สุราษฎร์ธานี", "เอสดีเอ็กซ์เพรส", true, 12, 590.0),
                SoItem(3002, "SO2026/05/29-13", "บจก. นครซีฟู้ดส์", "นครศรีธรรมราช", "เอสดีเอ็กซ์เพรส", false, 3, 120.0)
            )

            simRouteSosMap["กรุงเทพฯ-ปริมณฑล (BKK Metropolitans)"] = mutableListOf(
                SoItem(4001, "SO2026/05/29-01", "หจก. ศรีสมพร ออฟฟิศเสิร์ซ (สมุทรปราการ)", "สมุทรปราการ", "จัดส่งเอง (รถโรงงาน)", false, 2, 90.0),
                SoItem(4002, "SO2026/05/29-02", "บจก. สยามบรรจุภัณฑ์ (สาขา 2)", "กรุงเทพฯ", "จัดส่งเอง (รถโรงงาน)", false, 5, 230.0),
                SoItem(4003, "SO2026/05/29-05", "บจก. ยูเนี่ยน เคมีคอล อุตสาหกรรม", "นนทบุรี", "Kerry Express", true, 4, 180.0),
                SoItem(4004, "SO2026/05/29-10", "เจริญชัยฮาร์ดแวร์ สาขา 4", "ปทุมธานี", "Flash Express", true, 3, 130.0),
                SoItem(4005, "SO2026/05/29-14", "บจก. ไดนามิค สเตชั่นเนอร์รี่", "กรุงเทพฯ", "จัดส่งเอง (รถโรงงาน)", true, 1, 45.0)
            )
        }
    }

    // PERIODICALLY GENERATE EXTRA ORDER IN BACKGROUND (Simulating Polling)
    fun simulateLiveOrderArrival() {
        if (!settings.useSimulation) return
        synchronized(this@OdooRepository) {
            val routeNames = simRouteSosMap.keys.toList()
            if (routeNames.isEmpty()) return

            val chosenRoute = routeNames[Random.nextInt(routeNames.size)]
            val routeList = simRouteSosMap[chosenRoute] ?: return

            val randId = Random.nextInt(5000, 9999)
            val randSoNo = "SO2026/05/29-${Random.nextInt(15, 99)}"

            if (routeList.none { it.so == randSoNo }) {
                val carriers = listOf("จัดส่งเอง", "Kerry Express", "Flash Express", "นิ่มซี่เส็ง", "พาวเวอร์ขนส่ง")
                val customers = listOf("ร้านอุดมสุขบริการ", "บจก. กรุงเทพศิลา", "หจก. ไทยรวมสินธ์", "สมพงษ์ค้าเหล็ก", "บีเคเคพริ้นติ้ง")
                val provinces = listOf("กรุงเทพฯ", "นนทบุรี", "ปทุมธานี", "สมุทรปราการ", "พิจิตร", "นครสวรรค์", "เชียงใหม่")

                val newSo = SoItem(
                    soId = randId,
                    so = randSoNo,
                    customer = customers[Random.nextInt(customers.size)],
                    province = provinces[Random.nextInt(provinces.size)],
                    carrier = carriers[Random.nextInt(carriers.size)],
                    received = Random.nextBoolean(),  // Sometimes bill is received, sometimes not
                    packages = Random.nextInt(1, 8),
                    qty = Random.nextInt(2, 20) * 10.0
                )

                routeList.add(newSo)
                Log.d("OdooRepository", "Simulation: Added new live SO ${newSo.so} for route $chosenRoute")

                // Update route item count
                val pIdx = simRoutes.indexOfFirst { it.route == chosenRoute }
                if (pIdx != -1) {
                    val current = simRoutes[pIdx]
                    simRoutes[pIdx] = current.copy(soCount = routeList.size)
                }
            }
        }
    }


    /* ----------------------------------------------------
       BILL RECEIPT (MOBILE) API & SIMULATION METHODS
     ---------------------------------------------------- */

    suspend fun getPendingReceipts(): List<PendingReceipt> = withContext(Dispatchers.IO) {
        if (settings.useSimulation) {
            delay(800) // simulated loading
            synchronized(this@OdooRepository) {
                return@withContext ArrayList(simPendingReceipts)
            }
        } else {
            return@withContext getApi().getPendingReceipts().data ?: emptyList()
        }
    }

    suspend fun confirmBillReceipt(soIds: List<Int>, signerName: String, signatureBase64: String): ConfirmReceiptResponse = withContext(Dispatchers.IO) {
        if (settings.useSimulation) {
            delay(1200) // simulated upload
            if (soIds.isEmpty() || signerName.isEmpty()) {
                return@withContext ConfirmReceiptResponse(ok = false, detail = "กรุณาเลือกรายการและเซ็นชื่อผู้รับบิล")
            }

            var docNo = ""
            synchronized(this@OdooRepository) {
                // Remove confirmed orders from pending receipt list
                val confirmedSos = simPendingReceipts.filter { it.id in soIds }
                simPendingReceipts.removeAll { it.id in soIds }

                // Mark received in Dispatch routes so those matching SO numbers are flagged as received!
                simRouteSosMap.values.forEach { soList ->
                    for (i in 0 until soList.size) {
                        val so = soList[i]
                        if (confirmedSos.any { it.so == so.so }) {
                            soList[i] = so.copy(received = true)
                        }
                    }
                }

                docNo = "TR-2605${simReceiptCounter++}"
            }
            return@withContext ConfirmReceiptResponse(ok = true, docNo = docNo)
        } else {
            val req = ConfirmReceiptRequest(soIds, signatureBase64, signerName)
            return@withContext getApi().confirmReceipt(req)
        }
    }


    /* ----------------------------------------------------
       DISPATCH / LOADING (TABLET) API & SIMULATION METHODS
     ---------------------------------------------------- */

    suspend fun getDispatchRoutes(): List<RouteItem> = withContext(Dispatchers.IO) {
        if (settings.useSimulation) {
            delay(600)
            synchronized(this@OdooRepository) {
                // Update route counts dynamically
                for (i in 0 until simRoutes.size) {
                    val r = simRoutes[i]
                    simRoutes[i] = r.copy(soCount = simRouteSosMap[r.route]?.size ?: 0)
                }
                return@withContext ArrayList(simRoutes)
            }
        } else {
            return@withContext getApi().getDispatchRoutes().data ?: emptyList()
        }
    }

    suspend fun getRouteDetails(routeName: String): RouteDetailResponse = withContext(Dispatchers.IO) {
        if (settings.useSimulation) {
            delay(900)
            synchronized(this@OdooRepository) {
                val list = simRouteSosMap[routeName] ?: mutableListOf()
                return@withContext RouteDetailResponse(soCount = list.size, sos = ArrayList(list))
            }
        } else {
            val response = getApi().getRouteDetails(routeName)
            return@withContext response.data ?: RouteDetailResponse(0, emptyList())
        }
    }

    suspend fun confirmDispatch(
        routeName: String,
        soIds: List<Int>,
        plate: String,
        driver: String,
        departTime: String,
        notes: Map<String, String>
    ): ConfirmDispatchResponse = withContext(Dispatchers.IO) {
        if (settings.useSimulation) {
            delay(1500) // processing dispatch
            if (soIds.isEmpty() || plate.isEmpty() || driver.isEmpty()) {
                return@withContext ConfirmDispatchResponse(ok = false, detail = "กรุณาป้อนข้อมูล ทะเบียนรถ คนขับ และเลือกใบสั่งซื้อ")
            }

            synchronized(this@OdooRepository) {
                val routeList = simRouteSosMap[routeName]
                if (routeList != null) {
                    // Remove the dispatched ones
                    routeList.removeAll { it.soId in soIds }
                }
            }

            val docNo = "DO-2605${Random.nextInt(1000, 9999)}"
            return@withContext ConfirmDispatchResponse(
                ok = true,
                docNo = docNo,
                confirmed = soIds.size,
                pdfB64 = "MOCK_PDF_BASE64_FOR_DEMONSTRATION_PURPOSES",
                pdfName = "Odoo_Loading_Receipt_$docNo.pdf"
            )
        } else {
            val req = ConfirmDispatchRequest(
                route = routeName,
                soIds = soIds,
                plate = plate,
                driver = driver,
                departTime = departTime,
                notes = notes
            )
            return@withContext getApi().confirmDispatch(req)
        }
    }
}
