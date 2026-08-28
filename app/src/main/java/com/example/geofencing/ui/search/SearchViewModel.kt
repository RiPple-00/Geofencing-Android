package com.example.geofencing.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.mock.DashboardRepository
import com.example.geofencing.ui.mock.MockSector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 검색 결과(표시 + 이동 대상). 카트는 이동에 (섹터명, 카트id)가 필요해 함께 담는다.
sealed interface SearchResult {
    val label: String
    val hasViolation: Boolean

    data class Sector(val name: String, override val hasViolation: Boolean) : SearchResult {
        override val label: String get() = name
    }

    data class Cart(val sectorName: String, val cartId: String, override val hasViolation: Boolean) : SearchResult {
        override val label: String get() = cartId
    }
}

// 검색은 별도 API를 안 쓰고, DashboardRepository의 공유 캐시(전 섹터+카트)를 이름으로 필터한다.
@HiltViewModel
class SearchViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
    }

    val results: StateFlow<List<SearchResult>> =
        combine(_query, repository.observeSectors()) { q, state ->
            val sectors = (state as? LoadState.Success)?.data.orEmpty()
            searchResults(sectors, q)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

private fun searchResults(sectors: List<MockSector>, query: String): List<SearchResult> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    val sectorHits = sectors
        .filter { it.name.lowercase().contains(q) }
        .map { SearchResult.Sector(it.name, it.carts.any { c -> c.status == StatusKind.Violation }) }
    val cartHits = sectors.flatMap { sector ->
        sector.carts
            .filter { it.id.lowercase().contains(q) }
            .map { SearchResult.Cart(sector.name, it.id, it.status == StatusKind.Violation) }
    }
    return sectorHits + cartHits
}
