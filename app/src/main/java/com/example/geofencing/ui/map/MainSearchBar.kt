package com.example.geofencing.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.DarkBorderDefault
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkFillHighest
import com.example.geofencing.ui.theme.DarkTextDisabled
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Px3
import com.example.geofencing.ui.theme.PyMd
import com.example.geofencing.ui.theme.RoundedMd
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// 피그마 list_root/input/hamburger padding 값 반영, width는 100%로 설정.
// 블러 배경(list_root)은 상태바(sys_aos) 뒤까지 이어지도록 statusBarsPadding 없이 최상단부터 그리고,
// 실제 입력/햄버거 콘텐츠에만 statusBarsPadding을 줘서 상태바 아이콘과 안 겹치게 한다.
@Composable
fun MainSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onHamburgerClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onFocusChanged: (Boolean) -> Unit = {},
    searchResults: List<SearchResultItem> = emptyList(),
    onResultClick: (SearchResultItem) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) { onFocusChanged(isFocused) }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeEffect(
                    state = hazeState,
                    // 패널 fade 구간(진짜 투명)을 통해 지도가 비칠 때도 또렷하게 보이지 않도록
                    style = HazeStyle(
                        tint = HazeTint(Color.Black.copy(alpha = 0.40f)),
                        blurRadius = 3.dp
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(72.dp)
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = 1.5.dp,
                            color = if (isFocused) DarkBorderStrong else DarkBorderDefault,
                            shape = RoundedCornerShape(RoundedMd)
                        )
                        .background(color = DarkFillHighest, shape = RoundedCornerShape(RoundedMd))
                        .padding(horizontal = Px3, vertical = PyMd),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        // TODO: 상하 padding("unnamed") 정확한 값 확인되면 0.dp 대신 교체
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .padding(top = 0.dp, bottom = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_information),
                                contentDescription = null,
                                contentScale = ContentScale.None,
                                modifier = Modifier
                                    .padding(1.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = "Cart or Sector Name",
                                style = Body14,
                                color = DarkTextDisabled,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        // 비활성(포커스 아웃) 상태에서는 입력했던 텍스트가 회색(text-disabled)으로,
                        // 포커스 상태에서는 기본 텍스트 색으로 보인다 - 텍스트 자체는 지워지지 않음.
                        textStyle = Body14.copy(color = if (isFocused) DarkTextPrimary else DarkTextDisabled),
                        cursorBrush = SolidColor(DarkTextPrimary),
                        interactionSource = interactionSource
                    )
                }

                IconButton(
                    onClick = onHamburgerClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_hamburger),
                        contentDescription = "메뉴",
                        tint = DarkTextPrimary
                    )
                }
            }
        }

        if (isFocused && searchResults.isNotEmpty()) {
            SearchList(
                results = searchResults,
                onResultClick = onResultClick,
                hazeState = hazeState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainSearchBarPreview() {
    GeofencingTheme {
        MainSearchBar(
            query = "",
            onQueryChange = {},
            onHamburgerClick = {},
            hazeState = remember { HazeState() }
        )
    }
}
