# Geofencing Android

골프카트 지오펜싱/모니터링 Android 앱. **Site → Sector → Cart** 계층으로 카트의 위반(Violation)·연결끊김(Disconnect) 상태를 지도와 리스트로 모니터링한다. Jetpack Compose 기반.

> 📄 **상세 개발 문서**: [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md) — 환경/실행/아키텍처/빌드/테스트/디버깅/기술부채 전체.

## Setup

1. `local.properties.example`을 복사해 `local.properties`로 이름 변경.
2. Google Maps API 키를 발급받아 `MAPS_API_KEY` 채우기.
3. Supabase(임시 백엔드) `SUPABASE_URL`, `SUPABASE_ANON_KEY` 채우기.
4. (선택) 팀 REST BE를 별도 호스트에서 띄우면 `BASE_URL=http://<ip>:3000/api/` 추가. 기본값은 에뮬레이터→호스트 접근용 `http://10.0.2.2:3000/api/`.
5. Gradle sync → **Run ▶** 또는 `./gradlew installDebug`.

> `local.properties`, `keystore.properties`, `*.jks`는 gitignore 대상 — 커밋 금지. Supabase는 anon key만 사용한다.

## 기술 스택

- Kotlin, Jetpack Compose, Material 3 (다크 테마)
- Hilt (DI), Navigation Compose
- Google Maps Compose (`maps-compose`, `play-services-maps`, `android-maps-utils`)
- Retrofit, OkHttp, kotlinx.serialization (네트워킹)
- DataStore Preferences (로컬 저장)
- Haze (블러 효과)
- Supabase PostgREST (임시 백엔드)

## 요구 사항

- Android Studio (AGP 9.2 지원 버전) / JDK 17+ (빌드 실행은 AS 번들 JBR)
- Gradle 9.4.1, Kotlin 2.2.10, Compose BOM 2026.02
- compileSdk 37 / minSdk 26 / targetSdk 37

## 프로젝트 구조

```
app/src/main/java/com/example/geofencing
├── MainActivity.kt          # 단일 Activity → GeofencingNavHost
├── di/                      # Hilt 모듈 (App/Network/Supabase/Qualifiers)
├── data/                    # 도메인 모델·DataStore·Retrofit(REST BE 대비 계층)
│   ├── model/  local/  remote/(dto)  repository/
├── util/                    # 공용 유틸
└── ui/
    ├── navigation/            # GeofencingNavHost (현재 단일 main route)
    ├── home/                  # HomeScreen — WholeSector/Sector/Cart 탭 컨테이너
    ├── wholesector/ sector/ cart/   # 각 페이지 + ViewModel + 샘플데이터
    ├── map/                   # LiveGeofenceMap, GeofenceMapOverlay, 히트맵, 스냅샷, 검색
    ├── mock/                  # DashboardRepository + SupabaseDashboardRepository(현행)
    ├── components/  analytics/  common/  theme/
```

## 아키텍처 요약

- **단일 Activity + 단일 NavHost**. 화면 전환은 `HomeScreen` 내부 탭/페이지 상태로 처리(WholeSector ↔ Sector ↔ Cart).
- 각 페이지 = `@HiltViewModel` + `DashboardRepository`(현행 `SupabaseDashboardRepository`) 관찰 → `StateFlow<LoadState<T>>`(Loading/Success/Error) 단방향 흐름.
- 로컬 저장: DataStore(위반 확인 상태, 선택 사이트). Room 등 DB 없음.
- 데이터 레이어 이원화: `ui/mock`(임시 Supabase, 화면 구동) + `data/`(팀 REST BE 대비 계약). 팀 BE 확정 시 일원화 예정.

## 빌드

```bash
./gradlew assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease   # keystore.properties 있으면 릴리스 서명 → .../release/app-release.apk
```

## 테스트

```bash
./gradlew testDebugUnitTest
```

- `CartLabelVisibilityTest` — 마커 라벨 표시 임계 로직.
- `testutil/MainDispatcherRule` — `Dispatchers.Main`을 테스트 디스패처로 교체하는 공용 규칙.
- UI/E2E 테스트는 아직 없음(템플릿 수준). 커버리지 확충 필요.

## 디버깅

- Logcat 태그: `Analytics`(화면/이벤트), `OkHttp`(HTTP), `AndroidRuntime`(크래시).
- 지도가 빈 화면이면 `MAPS_API_KEY` 누락 또는 (Release) SHA-1 미등록 확인.
