package com.example.geofencing.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val geofence = viewModel.geofence

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(geofence?.name ?: "상세") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (geofence != null) {
                Text("위도: ${geofence.latitude}", style = MaterialTheme.typography.bodyLarge)
                Text("경도: ${geofence.longitude}", style = MaterialTheme.typography.bodyLarge)
                Text("반경: ${geofence.radiusMeters}m", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("해당 지오펜스를 찾을 수 없습니다.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
