package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.map.slidepanel.StatusCardContainer
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.DarkBorderDefault
import com.example.geofencing.ui.theme.DarkTextPrimary

// 3/4/5(Event Code, Driving Status, GeoFencing Status)가 공유하는 StatusCardContainer 사용.
// height는 200dp 고정(리스트 스크롤 영역 확보용) - 4/5번은 그래프 크기에 맞춰 따로 정함.
// events는 GET /sites/{siteId}/geofence-events 결과를 화면 표시용 문자열로 매핑한 것.
@Composable
fun EventCodeSection(modifier: Modifier = Modifier, events: List<String> = emptyList()) {
    StatusCardContainer(title = "Event Code", modifier = modifier.height(200.dp)) {
        val listState = rememberLazyListState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                items(events) { title ->
                    EventCodeRow(title = title)
                }
            }

            // 스크롤 가능함을 나타내는 스크롤바 인디케이터(실측 크기). 스크롤 위치가 맨
            // 위일 때의 썸 위치이므로 TopEnd - 중앙 정렬하면 리스트 항목과 동떨어져 보임.
            // 콘텐츠가 뷰포트를 넘지 않아 스크롤할 필요가 없으면 아예 그리지 않는다.
            if (listState.canScrollForward || listState.canScrollBackward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp)
                        .width(6.dp)
                        .height(88.dp)
                        .background(color = DarkBorderDefault, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
private fun EventCodeRow(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            // warning 아이콘 자체 padding(8dp=px2)만으로는 헤더 타이틀의 12dp 인셋과
            // 안 맞아서(8dp만 들어감) 4dp를 더해 8+4=12dp로 헤더와 맞춘다.
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.height(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = Body13,
                    color = DarkTextPrimary
                )
            }
        }

        // 화살표 실제 그려지는 크기는 5x10dp - ic_arrow_right.xml(16x16 캔버스) 안에
        // 그 비율로 이미 들어있어서 네이티브 크기(16dp)로 두면 그대로 맞음.
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(32.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
