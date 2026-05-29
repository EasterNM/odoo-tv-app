package com.example.data.api

import com.example.data.model.*
import retrofit2.http.*

interface OdooApi {

    @GET("api/mobile/pending-receipts")
    suspend fun getPendingReceipts(): CommonResponse<List<PendingReceipt>>

    @POST("api/mobile/confirm-receipt")
    suspend fun confirmReceipt(
        @Body request: ConfirmReceiptRequest
    ): ConfirmReceiptResponse

    @GET("api/dispatch/routes")
    suspend fun getDispatchRoutes(): CommonResponse<List<RouteItem>>

    @GET("api/dispatch/route")
    suspend fun getRouteDetails(
        @Query("name") name: String
    ): CommonResponse<RouteDetailResponse>

    @POST("api/dispatch/confirm")
    suspend fun confirmDispatch(
        @Body request: ConfirmDispatchRequest
    ): ConfirmDispatchResponse
}
