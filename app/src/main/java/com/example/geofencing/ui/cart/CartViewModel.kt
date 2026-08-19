package com.example.geofencing.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.mock.DashboardRepository
import com.example.geofencing.ui.mock.MockSector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 선택된 카트(select(sector, cartId))의 상세를 Repository에서 관찰해 CartPage UI state(LoadState)로 노출.
// 섹터를 관찰(observeSector)해 geofence·전체 carts까지 함께 담는다(지도용).
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    private val target = MutableStateFlow<Pair<String, String>?>(null) // (sectorName, cartId)
    private val retry = MutableStateFlow(0)

    fun select(sectorName: String, cartId: String) {
        target.value = sectorName to cartId
    }

    fun retry() {
        retry.value++
    }

    val state: StateFlow<LoadState<CartUiState>> =
        combine(target, retry) { t, _ -> t }
            .flatMapLatest { t ->
                if (t == null) flowOf<LoadState<CartUiState>>(LoadState.Loading)
                else repository.observeSector(t.first)
                    .map<MockSector?, LoadState<CartUiState>> { sector ->
                        if (sector == null) LoadState.Error("Sector not found: ${t.first}")
                        else LoadState.Success(cartStateFrom(sector, t.second))
                    }
                    .catch { emit(LoadState.Error(it.message ?: "Failed to load cart")) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}
