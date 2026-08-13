package com.example.geofencing.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.mock.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 선택된 카트(select(sector, cartId))의 상세를 Repository에서 관찰해 CartPage UI state로 노출.
// 섹터를 관찰(observeSector)해 geofence·전체 carts까지 함께 담는다(지도용).
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    private val target = MutableStateFlow<Pair<String, String>?>(null) // (sectorName, cartId)

    fun select(sectorName: String, cartId: String) {
        target.value = sectorName to cartId
    }

    val state: StateFlow<CartUiState?> = target
        .flatMapLatest { t ->
            if (t == null) flowOf(null)
            else repository.observeSector(t.first).map { s -> s?.let { cartStateFrom(it, t.second) } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
