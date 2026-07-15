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
├── di                     # Hilt 모듈 (AppModule, NetworkModule) + DataStore 한정자(Qualifiers)
├── data
│   ├── model                # Cart, Sector, SiteSummary 등 도메인 모델
│   ├── local                 # DataStore 기반 로컬 저장소 (위반 확인 상태, 선택된 사이트 등)
│   ├── remote                 # Retrofit API, DTO, DTO ↔ 도메인 모델 매퍼
│   │   └── dto
│   └── repository            # 데이터 소스 접근 (Cart, Sector, Site, SelectedSite, GeofenceEvent, ViolationAck)
├── util                   # 공용 유틸 (Pole of Inaccessibility 등)
└── ui
    ├── map               # 지도 화면 (검색, 마커, 슬라이드 패널)
    │   └── slidepanel          # 하단에서 드래그로 올라오는 패널(헤더, 탭)
    │       ├── cart              # Cart 탭 전용 카드/리스트
    │       ├── sector            # Sector 탭 전용 카드/리스트
    │       └── summary           # Summary 탭 전용 카드들
    ├── components        # 공용 컴포넌트 (Button, Card, SearchBar, InlineRetryNotice 등)
    ├── navigation        # 네비게이션 그래프
    └── theme             # 컬러/타이포그래피/스페이싱 등 공용 테마
```

#### 알려진 API 제약 / 필요한 BE 확장

아래 두 항목은 클라이언트만으로는 정확히 해결할 수 없어 BE 계약 변경이 필요합니다.

- `GET /sites/{siteId}/carts`에 `sectorId`, `violating` 필터 쿼리 파라미터가 없습니다. 그래서 클라이언트가 전체 페이지를 미리 다 가져온 뒤 직접 필터링/재-페이지네이션하고 있습니다(`MapViewModel.fetchAllCarts`, `MapScreen`의 `sectorScopedCartItems`/`filteredCartItems`). 필터 파라미터가 추가되면 서버 사이드 페이지네이션으로 전환할 수 있습니다.
- `CartListItemDto`/`Cart` 도메인 모델에 `sectorId` 필드가 없습니다. 그래서 섹터-카트 연결을 카트 이름에 섹터 이름이 포함되는지로 판단하는 부분 일치 방식으로 처리하고 있습니다(`MapScreen`의 `navigateToCartFilteredBySector`). 동명이거나 명명 규칙이 바뀌면 오작동할 수 있어 ID 기반 관계 필드가 필요합니다.

#### 오류/재시도 처리

- `MapViewModel`은 요약 로드·새로고침 실패를 `refreshError`(메시지), 검색 실패를 `searchError`(boolean), 폴링 연속 실패를 `hasPollFailure`로 상태만 노출합니다. 실제 화면 표현은 `ui/components/InlineRetryNotice.kt` 컴포넌트 하나로 분리되어 있어, 디자인이 확정되면 이 컴포넌트만 교체하면 됩니다.
- 검색 실패/폴링 실패는 아직 전용 UI 없이 상태만 기록됩니다(디자이너 미확정으로 이번 스코프에서 보류). 검색은 다음 입력 시 자연스럽게 재시도되고, 폴링은 다음 주기에 자동 재시도됩니다.
- `SelectedSiteRepository`는 로그인/사이트 선택 UI가 생기기 전까지 항상 siteId 1을 반환하는 DataStore 기반 리포지토리입니다. `MapViewModel`은 유스케이스(새로고침/폴링 1회/검색 1회) 시작 시점에 한 번만 읽어 그 실행 내부 모든 요청에 같은 siteId를 전달합니다(캐싱은 하지 않아 사이트 전환이 생겨도 다음 호출부터 자연스럽게 반영됩니다).

#### 테스트

- `./gradlew testDebugUnitTest`로 실행합니다.
- `MapViewModelTest` — Fake 리포지토리 기반으로 새로고침 성공/실패/재시도, 검색 실패/리셋, 실행 중 siteId 캡처 일관성을 검증합니다.
- `NextConsecutivePollFailuresTest` — 폴링 실패 판정 로직을 루프/딜레이와 분리한 순수 함수 단위로 검증합니다(무한 루프 자체의 타이밍 테스트는 후속 작업으로 남겨둠).
- `testutil/MainDispatcherRule` — `viewModelScope`가 쓰는 `Dispatchers.Main`을 테스트용 `TestDispatcher`로 교체하는 공용 JUnit 규칙입니다.