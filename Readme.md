# Geofencing Android

> 골프카트 지오펜싱/모니터링 Android 앱. **Site → Sector → Cart** 계층으로 카트의 위반(Violation)·연결끊김(Disconnect) 상태를 지도와 리스트로 모니터링한다. Jetpack Compose 기반.
>
> 개발/온보딩용 상세 문서. 세부 버전 값은 `app/build.gradle.kts` · `gradle/libs.versions.toml` 기준.

---

## 1. 프로젝트 기본 정보

| 항목 | 값 |
| --- | --- |
| App 이름 | `geofencing` (`res/values/strings.xml`의 `app_name`) |
| Package name / applicationId | `com.example.geofencing` |
| versionName / versionCode | `1.0` / `1` (`app/build.gradle.kts`) |
| 배포 APK 파일명 | `geofence_v1.2.apk` (Release 서명) |

> ⚠️ **버전 불일치 주의**: 배포 파일명은 `v1.2`이지만 앱 내부 `versionName`은 아직 `1.0`이다. 스토어 배포/업데이트 식별이 필요해지면 `versionName`/`versionCode`를 올려야 한다. (아래 10. 기술 부채 참고)

---

## 2. 개발 환경

| 항목 | 값 / 설명 |
| --- | --- |
| Android Studio | AGP 9.2를 지원하는 버전(최신 안정판 권장). Gradle JVM은 Android Studio 번들 JBR로 고정됨(`gradle.properties`의 `org.gradle.java.home`). |
| JDK | **빌드 실행**: Android Studio 번들 JBR(JDK 21). AGP 9.x는 최소 JDK 17 이상 필요. **앱 컴파일 타깃**: Java 11 (`sourceCompatibility`/`targetCompatibility = 11`). |
| Gradle | 9.4.1 (`gradle/wrapper/gradle-wrapper.properties`) |
| AGP (Android Gradle Plugin) | 9.2.1 |
| Kotlin | 2.2.10 (KSP 2.3.9) |
| Compose | BOM 2026.02.01, Material 3 |
| compileSdk / minSdk / targetSdk | 37 / 26 / 37 |

### 로컬 설정 파일

모두 **gitignore** 대상이며, `*.example` 템플릿을 복사해 채운다.

| 파일 | 용도 | 주요 키 |
| --- | --- | --- |
| `local.properties` | SDK 경로 + 시크릿. `local.properties.example` 복사. | `sdk.dir`, `MAPS_API_KEY`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`, (선택) `BASE_URL` |
| `keystore.properties` | Release 서명 정보. `keystore.properties.example` 복사. | `storeFile`, `storePassword`, `keyAlias`, `keyPassword` |

- `.env` / `secrets.properties`는 **사용하지 않는다.** 모든 시크릿은 `local.properties` → `BuildConfig`/`manifestPlaceholders`로 주입된다.
- Supabase는 **anon(publishable) key만** 넣는다. DB password / `service_role` key는 절대 커밋·기입 금지.

---

## 3. 실행 방법

### 로컬에서 Debug 실행

1. `cp local.properties.example local.properties`
2. Google Maps API 키 발급 후 `MAPS_API_KEY` 채우기.
3. Supabase 프로젝트의 `SUPABASE_URL`, `SUPABASE_ANON_KEY` 채우기(임시 백엔드).
4. (선택) 팀 REST BE를 실기기/별도 호스트에서 띄우면 `BASE_URL=http://<ip>:3000/api/` 추가. 기본값은 에뮬레이터→호스트 PC 접근용 `http://10.0.2.2:3000/api/`.
5. Android Studio에서 **Run ▶ (app)**, 또는 터미널에서:
   ```bash
   ./gradlew installDebug   # 연결된 기기/에뮬레이터에 설치
   # 또는
   ./gradlew assembleDebug  # app/build/outputs/apk/debug/app-debug.apk 생성
   ```

### 필요한 환경 변수

- 별도 OS 환경 변수는 **없다.** 모든 설정은 `local.properties`를 통해 `BuildConfig`로 들어간다.
- `BuildConfig` 필드: `BASE_URL`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`. Maps 키는 매니페스트 placeholder `${MAPS_API_KEY}`로 주입.

### 실행 전 준비 사항

- Android SDK **API 37** 설치.
- 유효한 **Google Maps API 키** (지도가 안 보이면 키 누락 또는 SHA-1 미등록 — Release는 릴리스 keystore의 SHA-1을 Google Cloud Console에 등록해야 함).
- **Supabase** 프로젝트(임시 백엔드) 접근 정보. 초기 스키마/목데이터는 `supabase/schema.sql` 참고(단일 파일).
- 인터넷 연결(권한: `INTERNET`).

---

## 4. 프로젝트 구조

```
app/src/main/java/com/example/geofencing
├── GeofencingApplication.kt     # @HiltAndroidApp 진입점
├── MainActivity.kt              # 단일 Activity, setContent → GeofencingNavHost
├── di/                          # Hilt 모듈
│   ├── AppModule.kt               # Repository 바인딩(@Binds), DataStore 제공
│   ├── NetworkModule.kt           # Retrofit/OkHttp/Json (팀 REST BE용)
│   ├── SupabaseModule.kt          # Supabase(PostgREST) API 제공
│   └── Qualifiers.kt              # DataStore 한정자
├── data/                        # "실 백엔드(REST)" 데이터 레이어 (팀 BE 대비, 도메인 계약 정의)
│   ├── model/                     # Cart, Sector, SiteSummary, GeofenceEventInfo 등 도메인 모델
│   ├── local/                     # DataStore (ViolationAck, SelectedSite)
│   ├── remote/                    # Retrofit API, DTO, GeoJson, DTO↔도메인 매퍼
│   │   ├── dto/
│   │   └── supabase/              # Supabase(PostgREST) 응답 DTO + API 인터페이스
│   └── repository/                # Cart/Sector/Site/GeofenceEvent/ViolationAck/SelectedSite (+Impl)
├── util/                        # PoleOfInaccessibility 등 공용 유틸
└── ui/
    ├── navigation/                # GeofencingNavHost (현재 단일 "main" route)
    ├── home/                      # HomeRoute(ViewModel 소유·배선) + HomeScreen(상태 없는 탭 컨테이너)
    ├── dashboard/                 # DashboardRepository(계약+MockDashboardRepository) · SupabaseDashboardRepository(현행) · DashboardModels · DashboardSampleData
    ├── wholesector/ · sector/ · cart/   # 화면별 Page + ViewModel + UiState + UiMapper + SampleData
    ├── map/                       # LiveGeofenceMap, GeofenceMapOverlay, 히트맵, SectorSnapshot(+Cache/Models), MapConfig, 검색
    ├── components/                # 공용 컴포넌트(AppTopBar, StatusListRow, ListSection, 아이콘버튼 등)
    ├── analytics/                 # AnalyticsLogger + DebugAnalyticsLogger(Logcat)
    ├── common/                    # LoadState (Loading/Success/Error)
    └── theme/                     # Color/Type/Spacing/Shape/ExtendedColors
```

### 핵심 클래스와 역할

| 클래스 | 역할 |
| --- | --- |
| `MainActivity` | 단일 Activity. `GeofencingNavHost`를 `setContent`로 표시. |
| `GeofencingNavHost` | Navigation Compose. 현재는 `main` 한 개 route(로그인/로딩 화면은 TODO). |
| `HomeRoute` | route 계층. `hiltViewModel()`로 세 ViewModel을 소유하고 상태/콜백(`HomeActions`)을 `HomeScreen`에 주입. |
| `HomeScreen` | **상태 없는(stateless)** 컨테이너. 상단바 + 섹터 탭(`SectorTabRow`) + 페이지 전환(WholeSector/Sector/Cart). |
| `WholeSectorViewModel` / `SectorViewModel` / `CartViewModel` | 각 화면의 `@HiltViewModel`. `DashboardRepository`를 관찰하고 `*UiMapper`로 변환해 `StateFlow<LoadState<*UiState>>` 노출. |
| `*UiState` / `*UiMapper` | 화면별 상태 모델과 도메인(`DashboardSector`)→UI 변환 로직. Compose에서 분리되어 단위 테스트 가능. |
| `DashboardRepository` (`ui/dashboard`) | 대시보드 데이터 계약(`observeSectors/observeSector/observeCart/refresh`). `MockDashboardRepository`(로컬 목) 구현 병존. |
| `SupabaseDashboardRepository` (`ui/dashboard`) | **현행 구현.** Supabase(PostgREST)에서 섹터/카트/이벤트를 읽어 `DashboardSector`로 조립, `LoadState`로 흘려보냄. |
| `LiveGeofenceMap` | GoogleMap(카메라: 카트 추적 `FollowCart` / 지오펜스 맞춤 `FitGeofence`) 위에 `GeofenceMapOverlay`(Canvas 마커/글로우/라벨)를 라이브 projection으로 겹침. |

### 화면 / ViewModel / Repository / API 구조

```
Supabase (PostgREST, 임시 BE)
        │  SupabaseApi (Retrofit, data/remote/supabase)
        ▼
SupabaseDashboardRepository (ui/dashboard)
        │  LoadState<List<DashboardSector>>
        ▼
WholeSector/Sector/Cart ViewModel ──(*UiMapper)──▶ *UiState ──(StateFlow)──▶ *Page (Compose)
        │
        └─ (팀 REST BE 도입 시) data/remote GeofencingApi + data/repository 로 교체 예정
```

---

## 5. 아키텍처 설명

- **UI 구조**: 100% Jetpack Compose + Material 3. **단일 Activity, 단일 NavHost**. 화면 전환은 route가 아니라 `HomeScreen` 내부의 **탭/페이지 상태**로 처리(WholeSector ↔ Sector ↔ Cart). 다크 테마 전용, 커스텀 `ExtendedColors`/`Type`/`Spacing`.
- **화면 계층 분리**: 각 화면 = `Page`(Compose UI) + `ViewModel` + `UiState`(화면 상태 모델) + `UiMapper`(도메인→UI 변환) + `SampleData`(프리뷰용). `HomeRoute`가 ViewModel을 소유하고, 상태를 없는(stateless) `HomeScreen`에 주입한다(상태 호이스팅).
- **ViewModel 사용 방식**: `HomeRoute`가 `hiltViewModel()`로 주입, ViewModel은 Repository의 `Flow`를 `*UiMapper`로 `map`해 `stateIn(viewModelScope, WhileSubscribed(5s), Loading)`으로 화면 상태를 만든다. UI는 `collectAsState()`로 구독. 재시도는 `retry()` → `repository.refresh()`.
- **Repository 구조**: 계약(interface)과 구현(Impl) 분리, Hilt `@Binds`로 연결. **두 계층**이 공존한다.
  - `ui/dashboard`의 `DashboardRepository` → **현행** `SupabaseDashboardRepository`(임시 Supabase 백엔드)가 실제 화면을 구동. `MockDashboardRepository`(로컬 목)도 있음.
  - `data/`의 Retrofit 기반 `CartRepository`/`SectorRepository`/… → **팀 REST BE 도입 대비** 도메인 계약. 현재 대시보드 경로에서는 주 사용 안 함.
- **로컬 저장소 사용 여부**: **사용함.** DataStore Preferences — `ViolationAckDataStore`(위반 확인 상태), `SelectedSiteDataStore`(선택 사이트). Room 등 DB는 없음.
- **화면 이동 방식**: Navigation Compose 골격은 있으나 현재 route는 `main` 하나. 실질 이동은 탭/페이지 상태 + 콜백(`HomeActions`의 `onSelectSector`/`onSelectCart` 등)으로 상위 컨테이너가 페이지를 전환.
- **상태 관리 방식**: Kotlin `Flow`/`StateFlow` + `LoadState<T>`(`Loading`/`Success`/`Error`) 단방향 데이터 흐름. 에러 시 `LoadState.Error` → 재시도 노출. 지도 카메라/오버레이는 Compose 상태(`rememberCameraPositionState`, `snapshotFlow`)로 반응형 갱신.

---

## 6. 권한 및 외부 SDK

### Android 권한 목록

| 권한 | 용도 |
| --- | --- |
| `android.permission.INTERNET` | 지도 타일, Supabase/REST API 통신 |

> 위치(`ACCESS_FINE_LOCATION` 등) 권한은 사용하지 않는다 — 카트 위치는 서버에서 받아 표시하며, 기기 GPS를 쓰지 않는다.
> Debug 빌드는 `usesCleartextTraffic="true"`(로컬 HTTP BE 테스트용), Release는 `"false"`.

### 주요 외부 SDK / 라이브러리

| 분류 | 라이브러리 |
| --- | --- |
| 지도 | `maps-compose` 8.3.0, `play-services-maps` 20.0.0, `android-maps-utils` 4.0.0 |
| DI | Hilt 2.60.1 (+ hilt-navigation-compose) |
| 네트워킹 | Retrofit 3.0.0, OkHttp 5.1.0(+logging), kotlinx.serialization 1.9.0 |
| 비동기 | kotlinx-coroutines 1.10.2 |
| 로컬 저장 | DataStore Preferences 1.1.1 |
| UI/효과 | Navigation Compose 2.9.8, Haze 1.7.2(블러) |
| 백엔드(임시) | Supabase PostgREST (Retrofit로 접근) |

---

## 7. APK 빌드 방법

### Debug APK

```bash
./gradlew assembleDebug
# 산출물: app/build/outputs/apk/debug/app-debug.apk (자동 debug 서명)
```

### Release APK

1. 릴리스 keystore 준비 후 `keystore.properties` 작성(`keystore.properties.example` 참고). `*.jks`와 `keystore.properties`는 **커밋 금지**.
2. 빌드:
   ```bash
   ./gradlew assembleRelease
   # 산출물: app/build/outputs/apk/release/app-release.apk
   ```
- `keystore.properties`의 4개 값이 모두 채워지고 keystore 파일이 실제 존재하면 **릴리스 키로 서명**된다. 하나라도 빠지면(예: 시크릿 없는 CI) 서명 없이 빌드되어 빌드 자체는 실패하지 않는다(`hasReleaseSigning` 로직).
- 서명 검증: `apksigner verify app-release.apk`.
- Release 지도가 안 보이면: 릴리스 keystore의 **SHA-1**을 Google Cloud Console의 Maps 키에 등록. Debug↔Release 서명 충돌 시 기존 앱 삭제 후 재설치.
- `isMinifyEnabled = false` (현재 R8 미적용). ProGuard 규칙은 `app/proguard-rules.pro`.

---

## 8. 테스트 설명

| 종류 | 존재 여부 | 내용 |
| --- | --- | --- |
| Unit Test | ✅ | 디자인 독립적인 코어 로직/매퍼를 커버. `LoadStateTest`(LoadState.map), `GeoJsonRingTest`(GeoJSON 링 변환), `ParseInstantTest`(타임스탬프 파싱), `FormatDurationTest`(지속시간 포맷), `PaginationItemsTest`(페이지네이션), `DashboardUiMapperTest`(도메인→UI 매핑), `CartLabelVisibilityTest`(마커 라벨 임계). + `testutil/MainDispatcherRule`. |
| UI Test (Instrumented) | ▲ (템플릿만) | `ExampleInstrumentedTest` — 기본 생성 템플릿 수준. 실제 화면 테스트 없음. |
| E2E Test | ❌ | 없음. |

```bash
./gradlew testDebugUnitTest              # 단위 테스트
./gradlew connectedDebugAndroidTest      # 계측 테스트(기기/에뮬레이터 필요)
```

> 코어 순수 로직(매퍼/포맷/파싱/페이지네이션)은 커버되나, ViewModel·Repository 통합 및 UI/E2E 테스트는 아직 없어 확충이 필요하다(10. 기술 부채).

---

## 9. 문제 분석 및 디버깅 방법

### 주요 Logcat 태그

| 태그 | 출처 | 내용 |
| --- | --- | --- |
| `Analytics` | `DebugAnalyticsLogger` | 화면 진입/이벤트 로그(`screen: ...`, `event: ...`). 임시 구현 — 실제 전송 대신 Logcat에만 출력. |
| `OkHttp` | OkHttp `HttpLoggingInterceptor` | HTTP 요청/응답 로그(Supabase/REST 통신 디버깅). |
| `AndroidRuntime` | 시스템 | `FATAL EXCEPTION` — 앱 크래시 스택트레이스. |

### 앱 실행 오류 확인 방법

```bash
adb logcat --pid=$(adb shell pidof com.example.geofencing)      # 앱 프로세스 로그만
adb logcat *:E                                                  # 에러 레벨만
adb logcat | grep -E "AndroidRuntime|Analytics|OkHttp"          # 크래시/이벤트/HTTP
```

- **지도가 빈 화면**: `MAPS_API_KEY` 누락 또는 (Release) SHA-1 미등록. `local.properties`/Cloud Console 확인.
- **데이터 안 뜸**: `SUPABASE_URL`/`SUPABASE_ANON_KEY` 확인, `OkHttp` 로그로 응답 코드 점검. UI는 `LoadState.Error`로 재시도 노출.
- **설치 실패("패키지가 잘못되어")**: Debug↔Release 서명 충돌. 기존 앱 삭제 후 재설치.

---

## 10. 기술 부채

### 리팩터링이 필요한 부분

- **`applicationId`가 기본값 `com.example.geofencing`** — 실배포 전 실제 도메인 기반 ID로 변경 필요.
- **버전 관리**: `versionName`이 배포 파일명(v1.2)과 불일치(1.0 고정). 릴리스 프로세스에서 버전 자동/수동 갱신 규칙 필요.
- **데이터 레이어 이원화**: `ui/dashboard`의 Supabase 경로가 실제 화면을 구동하고, `data/`의 Retrofit 계층은 팀 REST BE 대비로 병존. 팀 BE 확정 시 `DashboardRepository` 구현을 교체하고 `data/`로 일원화 필요.
- **네비게이션**: NavHost route가 `main` 하나뿐이고 화면 전환이 `HomeScreen` 내부 상태에 묶여 있음. 로그인/로딩 화면 도입 시 route 기반으로 재정비 필요(코드 내 TODO).
- **Analytics**: `DebugAnalyticsLogger`는 Logcat 출력 스캐폴딩. 실제 수집 필요 시 실 구현체로 `@Binds` 교체.
- **테스트 커버리지**: 코어 로직/매퍼는 단위 테스트로 커버되나 ViewModel·Repository 통합 및 UI/E2E는 없음 — 확충 필요.

### 이후 유지보수 시 주의할 사항

- **시크릿 관리**: `local.properties`, `keystore.properties`, `*.jks`는 절대 커밋 금지(gitignore 유지). Supabase는 anon key만 사용, `service_role`/DB password 금지.
- **임시 백엔드 전제**: 현재 Supabase는 인터임(interim). 스키마/목데이터는 `supabase/schema.sql` 단일 파일로 관리(신규 프로젝트에 한 번 실행).
- **목 데이터 vs 실 API 격차**: 초기(7월) 팀 REST API는 `geofenceStatus`가 `violating`/`compliant`뿐이다. 앱의 **Disconnect 상태·카트 위치(lat/lng)·위반 상세(속도/주소)는 이 계약에 없는 목(mock) 실험 기능**이므로, 실 BE 연동 시 이 차이를 조정해야 한다.
- **지도 튜닝 값**: 마커 크기/줌 한계(`CartMinZoom`, `HeatmapMin/MaxZoom`, 마커 min/max·glow 등)는 Figma 실측 기반 상수. 실기기에서 재확인하며 조정.
- **SDK 37**: compile/target이 최신 API에 맞춰져 있어, Android Studio/AGP/JDK 버전 요구를 함께 유지해야 함(AGP 9 → JDK 17+).
- **로그인/로그아웃/알림**: 기획 미확정으로 보류 중(별도 브랜치). 확정 전까지 관련 UI/route는 미완성 상태.
