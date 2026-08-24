package com.example.geofencing.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.ui.components.AppSearchBar
import com.example.geofencing.ui.components.noRippleClickable
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.extendedColors

// 상단 검색 버튼으로 여는 전체화면 검색 오버레이. 입력(AppSearchBar) + 결과(섹터/카트) 리스트.
// 결과 클릭은 상위(HomeScreen)가 탭 전환/카트 드릴다운으로 처리한다. 뒤로가기/취소로 닫힘.
@Composable
fun SearchOverlay(
    onSectorSelect: (String) -> Unit,
    onCartSelect: (sectorName: String, cartId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.extendedColors
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val focusRequester = remember { FocusRequester() }

    BackHandler(onBack = onDismiss)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppSearchBar(
                query = query,
                onQueryChange = viewModel::setQuery,
                placeholder = "Cart 또는 Sector 이름",
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            TextButton(onClick = onDismiss) {
                Text(text = "취소", style = Body14, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                query.isBlank() -> Unit
                results.isEmpty() -> Text(
                    text = "검색 결과가 없습니다.",
                    style = Body14,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(results) { result ->
                        SearchResultRow(
                            result = result,
                            onClick = {
                                when (result) {
                                    is SearchResult.Sector -> onSectorSelect(result.name)
                                    is SearchResult.Cart -> onCartSelect(result.sectorName, result.cartId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(result: SearchResult, onClick: () -> Unit) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = result.label,
            style = Label16,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (result.hasViolation) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.criticalPrimary)
            )
        }
        Text(
            text = if (result is SearchResult.Cart) "Cart" else "Sector",
            style = Body13,
            color = colors.textSecondary
        )
    }
}
