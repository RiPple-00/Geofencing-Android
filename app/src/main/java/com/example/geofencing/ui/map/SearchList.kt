package com.example.geofencing.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.DarkBorderFocus
import com.example.geofencing.ui.theme.DarkBrandPrimary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.Label16
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// 검색어 입력 시 searchBar 바로 아래에 여백 없이 붙는 검색 결과 드롭다운.
// searchBar와 같은 좌우 16dp 여백을 써서 searchBar 폭 기준 중앙 정렬이 되도록 한다.
@Composable
fun SearchList(
    results: List<SearchResultItem>,
    onResultClick: (SearchResultItem) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    tint = HazeTint(Color.Black.copy(alpha = 0.60f)),
                    blurRadius = 3.dp
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        results.forEachIndexed { index, result ->
            SearchListItemRow(
                result = result,
                showBottomBorder = index < results.lastIndex,
                onClick = { onResultClick(result) }
            )
        }
    }
}

@Composable
private fun SearchListItemRow(
    result: SearchResultItem,
    showBottomBorder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (showBottomBorder) {
                    it.drawBehind {
                        drawLine(
                            color = DarkBorderFocus,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                } else {
                    it
                }
            }
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (result.hasViolation) {
                    Image(
                        painter = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_ok),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(DarkBrandPrimary),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = result.name,
                    style = Label16,
                    color = DarkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .heightIn(min = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_arrow_right_16),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
