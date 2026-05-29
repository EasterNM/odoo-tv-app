package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocalSettings
import com.example.data.model.PendingReceipt
import com.example.data.model.RouteItem
import com.example.data.model.RouteDetailResponse
import com.example.data.repository.OdooRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OdooViewModel(application: Application) : AndroidViewModel(application) {

    val settings = LocalSettings(application)
    val repository = OdooRepository(settings)

    // Common Settings state
    val baseUrl = MutableStateFlow(settings.baseUrl)
    val useSimulation = MutableStateFlow(settings.useSimulation)

    // Bill receipt state
    private val _pendingReceipts = MutableStateFlow<List<PendingReceipt>>(emptyList())
    val pendingReceipts: StateFlow<List<PendingReceipt>> = _pendingReceipts.asStateFlow()

    private val _receiptLoading = MutableStateFlow(false)
    val receiptLoading: StateFlow<Boolean> = _receiptLoading.asStateFlow()

    private val _receiptError = MutableStateFlow<String?>(null)
    val receiptError: StateFlow<String?> = _receiptError.asStateFlow()

    // Dispatch routes state
    private val _routes = MutableStateFlow<List<RouteItem>>(emptyList())
    val routes: StateFlow<List<RouteItem>> = _routes.asStateFlow()

    private val _routesLoading = MutableStateFlow(false)
    val routesLoading: StateFlow<Boolean> = _routesLoading.asStateFlow()

    private val _routesError = MutableStateFlow<String?>(null)
    val routesError: StateFlow<String?> = _routesError.asStateFlow()

    // Selected Route details state
    private val _routeDetails = MutableStateFlow<RouteDetailResponse?>(null)
    val routeDetails: StateFlow<RouteDetailResponse?> = _routeDetails.asStateFlow()

    private val _detailsLoading = MutableStateFlow(false)
    val detailsLoading: StateFlow<Boolean> = _detailsLoading.asStateFlow()

    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError: StateFlow<String?> = _detailsError.asStateFlow()

    // Polling job
    private var pollingJob: Job? = null
    val toastMessage = MutableStateFlow<String?>(null)

    init {
        loadReceipts()
        loadRoutes()
    }

    fun updateSettings(url: String, sim: Boolean) {
        settings.baseUrl = url
        settings.useSimulation = sim
        baseUrl.value = url
        useSimulation.value = sim
        repository.resetSimulationData()
        loadReceipts()
        loadRoutes()
    }

    fun resetSimulation() {
        repository.resetSimulationData()
        loadReceipts()
        loadRoutes()
    }

    // Load Pending Bills
    fun loadReceipts() {
        viewModelScope.launch {
            _receiptLoading.value = true
            _receiptError.value = null
            try {
                _pendingReceipts.value = repository.getPendingReceipts()
            } catch (e: Exception) {
                Log.e("OdooViewModel", "Failed to load receipts", e)
                _receiptError.value = e.message ?: "พบข้อผิดพลาดในการโหลดรายการ"
            } finally {
                _receiptLoading.value = false
            }
        }
    }

    // Load Routes
    fun loadRoutes() {
        viewModelScope.launch {
            _routesLoading.value = true
            _routesError.value = null
            try {
                _routes.value = repository.getDispatchRoutes()
            } catch (e: Exception) {
                Log.e("OdooViewModel", "Failed to load routes", e)
                _routesError.value = e.message ?: "พบข้อผิดพลาดในการโหลดข้อมูลเส้นทาง"
            } finally {
                _routesLoading.value = false
            }
        }
    }

    // Select Route details
    fun loadRouteDetails(routeName: String) {
        viewModelScope.launch {
            _detailsLoading.value = true
            _detailsError.value = null
            try {
                _routeDetails.value = repository.getRouteDetails(routeName)
            } catch (e: Exception) {
                Log.e("OdooViewModel", "Failed to load route details", e)
                _detailsError.value = e.message ?: "พบข้อผิดพลาดในการโหลดรายละเอียด"
            } finally {
                _detailsLoading.value = false
            }
        }
    }

    // Start live polling for routes
    fun startPolling() {
        stopPolling()
        if (!settings.useSimulation) return // polling only supported inside simulation or if backend supports it
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(12000) // Poll every 12 seconds
                repository.simulateLiveOrderArrival()
                // Refresh lists
                loadRoutes()
                val currentRoute = _routeDetails.value
                // If a route details screen is active, refresh details
                routeDetails.value?.let {
                    // Find active route details name
                    // Here we find route details for current active table
                }
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
