#### Setup

1. `local.properties.example`을 복사해서 `local.properties`로 이름 변경
2. Google Maps API 키를 발급받아 `MAPS_API_KEY` 값 채우기
3. (선택) BE 서버를 실기기/별도 호스트에서 띄우는 경우 `local.properties`에 `BASE_URL=http://<ip>:3000/api/` 추가. 기본값은 에뮬레이터에서 호스트 PC에 접근하는 `http://10.0.2.2:3000/api/`
4. Gradle sync

#### 기술 스택

- Kotlin, Jetpack Compose, Material 3
- Hilt (DI), Navigation Compose
- Google Maps Compose
- Retrofit, OkHttp, kotlinx.serialization (네트워킹)
- DataStore Preferences (로컬 저장)
- Haze (블러 효과)

#### 요구 사항

- Android Studio (최신 안정 버전 권장)
- minSdk 26 / targetSdk 37 / compileSdk 37

#### 프로젝트 구조

```
app/src/main/java/com/example/geofencing
├── di                     # Hilt 모듈 (AppModule, NetworkModule)
├── data
│   ├── model                # Cart, Sector, SiteSummary 등 도메인 모델
│   ├── local                 # DataStore 기반 로컬 저장소 (위반 확인 상태 등)
│   ├── remote                 # Retrofit API, DTO, DTO ↔ 도메인 모델 매퍼
│   │   └── dto
│   └── repository            # 데이터 소스 접근 (Cart, Sector, Site, GeofenceEvent, ViolationAck)
├── util                   # 공용 유틸 (Pole of Inaccessibility 등)
└── ui
    ├── map               # 지도 화면 (검색, 마커, 슬라이드 패널)
    │   └── slidepanel          # 하단에서 드래그로 올라오는 패널(헤더, 탭)
    │       ├── cart              # Cart 탭 전용 카드/리스트
    │       ├── sector            # Sector 탭 전용 카드/리스트
    │       └── summary           # Summary 탭 전용 카드들
    ├── components        # 공용 컴포넌트 (Button, Card, SearchBar 등)
    ├── navigation        # 네비게이션 그래프
    └── theme             # 컬러/타이포그래피/스페이싱 등 공용 테마
```