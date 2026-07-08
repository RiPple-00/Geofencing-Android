package com.example.geofencing.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.data.model.Geofence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGeofenceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val geofences by viewModel.geofences.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("지오펜스 목록") }) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(geofences, key = { it.id }) { geofence ->
                GeofenceRow(
                    geofence = geofence,
                    onClick = { onGeofenceClick(geofence.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun GeofenceRow(geofence: Geofence, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = geofence.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "반경 ${geofence.radiusMeters.toInt()}m",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
