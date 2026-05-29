package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PendingReceipt(
    @Json(name = "id") val id: Int,
    @Json(name = "so") val so: String,
    @Json(name = "easy_no") val easyNo: String?,
    @Json(name = "customer") val customer: String,
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class CommonResponse<T>(
    @Json(name = "data") val data: T? = null,
    @Json(name = "ok") val ok: Boolean? = null,
    @Json(name = "detail") val detail: String? = null
)

@JsonClass(generateAdapter = true)
data class ConfirmReceiptRequest(
    @Json(name = "so_ids") val soIds: List<Int>,
    @Json(name = "signature_b64") val signatureB64: String,
    @Json(name = "signer_name") val signerName: String
)

@JsonClass(generateAdapter = true)
data class ConfirmReceiptResponse(
    @Json(name = "ok") val ok: Boolean = true,
    @Json(name = "doc_no") val docNo: String? = null,
    @Json(name = "detail") val detail: String? = null
)

@JsonClass(generateAdapter = true)
data class RouteItem(
    @Json(name = "route") val route: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "color") val color: String,
    @Json(name = "so_count") val soCount: Int
)

@JsonClass(generateAdapter = true)
data class SoItem(
    @Json(name = "so_id") val soId: Int,
    @Json(name = "so") val so: String,
    @Json(name = "customer") val customer: String,
    @Json(name = "province") val province: String,
    @Json(name = "carrier") val carrier: String,
    @Json(name = "received") val received: Boolean,
    @Json(name = "packages") val packages: Int,
    @Json(name = "qty") val qty: Double
)

@JsonClass(generateAdapter = true)
data class RouteDetailResponse(
    @Json(name = "so_count") val soCount: Int,
    @Json(name = "sos") val sos: List<SoItem>
)

@JsonClass(generateAdapter = true)
data class ConfirmDispatchRequest(
    @Json(name = "route") val route: String,
    @Json(name = "so_ids") val soIds: List<Int>,
    @Json(name = "plate") val plate: String,
    @Json(name = "driver") val driver: String,
    @Json(name = "depart_time") val departTime: String,
    @Json(name = "notes") val notes: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class ConfirmDispatchResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "doc_no") val docNo: String? = null,
    @Json(name = "confirmed") val confirmed: Int? = null,
    @Json(name = "pdf_b64") val pdfB64: String? = null,
    @Json(name = "pdf_name") val pdfName: String? = null,
    @Json(name = "detail") val detail: String? = null
)
