package com.example.geofencing.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.geofencing.ui.cart.CartViewModel
import com.example.geofencing.ui.sector.SectorViewModel
import com.example.geofencing.ui.wholesector.WholeSectorViewModel

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    wholeSectorViewModel: WholeSectorViewModel = hiltViewModel(),
    sectorViewModel: SectorViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    onBellClick: () -> Unit = {}
) {
    val wholeSectorState by wholeSectorViewModel.state.collectAsState()
    val sectorState by sectorViewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()

    HomeScreen(
        wholeSectorState = wholeSectorState,
        sectorState = sectorState,
        cartState = cartState,
        modifier = modifier,
        onSelectSector = sectorViewModel::select,
        onSelectCart = cartViewModel::select,
        onWholeSectorRetry = wholeSectorViewModel::retry,
        onSectorRetry = sectorViewModel::retry,
        onCartRetry = cartViewModel::retry,
        onBellClick = onBellClick
    )
}
