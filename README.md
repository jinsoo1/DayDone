# DayDone (데이던)

**오늘 얼마까지 써도 되는지** 알려주는 1인 가구용 생활비 앱입니다.

복잡한 가계부가 아니라, 월 수입에서 저축·고정비·미래에 나갈 큰 지출을 **먼저 빼둔 뒤** 남은 생활비를 남은 날수로 나눠 "오늘 이만큼 써도 괜찮아요" 금액 하나를 보여줍니다. 오늘 조금 넘겨도 다음 날 자동으로 다시 나누기 때문에, 한 번 무너졌다고 그 달을 포기할 필요가 없습니다.

소비를 **분석**하는 앱이 아니라, 오늘 쓸 돈을 **지켜주는** 앱을 목표로 합니다.

서버·회원가입·권한이 모두 없는 로컬 전용 앱이며, 최신 안드로이드 스택(Compose, Coroutines/Flow, Hilt, Room)으로 1인 개발하고 있습니다.

> [Google Play에서 보기](https://play.google.com/store/apps/details?id=com.jsworld.android.daydone)

---

## 주요 기능

- **오늘 권장 금액** — 남은 순수 생활비 ÷ 남은 일수. 초과해도 경고하지 않고 남은 날 기준으로 자동 재분배
- **예정 차감** — 이번 기간에 나갈 저축·고정비를 실제 출금 전에 미리 제외. 금액은 월별로 이월되어 과거 기록이 보존됨
- **금고 (미래 지출 준비)** — 자동차세·보험료·기념일처럼 큰 지출을 목표월까지 매달 나눠 준비하도록 안내하고, 납부월엔 부족분만 차감
- **월별 예산** — 달마다 다른 예산을 기록하고, 값이 없는 달은 직전 값을 이월
- **월 지출 탭** — 예산 기간 전체를 캘린더로 보고 날짜별 내역 확인
- **기간 리포트** — 예산 페이스, 하루 평균·무지출 일수·필수 비중, 자주 쓴 곳(카테고리 자동 분류), 남은 기간 가이드, **지난 기간 같은 시점 대비 비교**, 기간 종료 후 결산 리포트
- **무지출 챌린지** — 완전 무지출 / 필수 지출 허용 / 상한 3가지 모드, 도전 기록 보관
- **엑셀 내보내기** — 예산월별로 정리된 지출 시트 + 고정지출 시트, xlsx 한 파일
- **백업 / 복원** — 전체 데이터를 JSON 파일로 저장하고 되살리기
- **공지사항** — 앱 내 업데이트 안내

### 설계상 특징

- **입력 마찰 최소화** — 지출 입력은 이름과 금액 두 개뿐. 카테고리는 내장 키워드 사전으로 자동 분류(16종)
- **권한 0개** — 요청하는 안드로이드 권한이 하나도 없습니다. 서버가 없어 기록이 기기 밖으로 나가지 않습니다
- **의존성 최소화** — 엑셀(xlsx) 생성은 Apache POI 같은 무거운 라이브러리 대신, xlsx가 XML을 담은 ZIP이라는 점을 이용해 직접 구현했습니다

---

## 기술 스택

- **Language** : Kotlin
- **UI** : Jetpack Compose, Material 3, Navigation Compose
- **Architecture** : MVVM + UseCase 분리 (data / domain / presentation)
- **Async** : Coroutines, Flow
- **DI** : Hilt (KSP)
- **Local DB** : Room
- **Preferences** : DataStore
- **Date** : java.time
- **Excel** : 자체 구현 (`data/excel/XlsxWriter`, 외부 의존성 없음)
- **Test** : JUnit4 (계산 로직 유닛 테스트 66개)

---

## 아키텍처

데이터 흐름은 `Room → Repository → UseCase → ViewModel → UiState` 를 따릅니다.
계산 로직은 ViewModel에 두지 않고 **UseCase로 분리**해 순수 함수로 테스트합니다.

```
com.jsworld.android.daydone
├─ data           # Room(dao·entity·db), DataStore, Repository 구현, Mapper, Excel
├─ domain         # 도메인 모델, Repository 인터페이스, UseCase(계산 로직)
├─ presentation   # Compose 화면·ViewModel (today / monthly / vault / report / challenge / settings …)
├─ navigation     # 네비게이션 그래프
├─ di             # Hilt 모듈
└─ ui             # 공용 컴포넌트, 테마
```

- **domain** 은 안드로이드 프레임워크에 의존하지 않는 순수 계층입니다. 기간 계산·권장 금액·리포트 집계·카테고리 분류가 모두 여기 UseCase에 있습니다.
- **data** 는 domain 인터페이스를 구현하고 Room·DataStore를 다룹니다.
- **presentation** 은 ViewModel이 UseCase를 조합해 UiState를 만들고 Compose가 그립니다.

### 핵심 도메인 규칙

- **예산 기간** — 사용자는 시작일만 정하고 종료일은 다음 기간 시작 전날로 자동 계산됩니다. 시작일이 그 달에 없는 날짜(29~31일)면 말일로 clamp되어, 말일 시작도 지원합니다.
- **anchorMonth 불변식** — 시작일이 전역 단일값이라 "한 달에 기간은 정확히 하나"가 성립하고, `"YYYY-MM"` 키가 기간의 유일 식별자가 됩니다. 월별 예산과 고정비 금액 이월이 이 키에 의존합니다.
- **금고 불변식** — 준비금은 `ExpenseEntity(type=FUTURE_PREPARE)` 지출 한 줄이 유일한 진실원이고, 금고 잔액은 그 합으로 파생됩니다(이중 계산 방지).

---

## 개발 환경

- **minSdk** : 28
- **targetSdk / compileSdk** : 36
- **JDK** : 17
- **AGP / Gradle** : 9.2.1 / 9.4.1
- **Kotlin** : 2.3.0 (AGP 내장 Kotlin 사용, KSP)

## 빌드 & 실행

```bash
# 저장소 클론 후
./gradlew :app:assembleDebug

# 계산 로직 테스트
./gradlew :app:testDebugUnitTest
```

Android Studio에서 프로젝트를 열면 Gradle Sync 후 바로 실행할 수 있습니다.
릴리즈 서명은 루트의 `keystore.properties`(git 미포함)에서 읽으며, 파일이 없으면 서명 없이 빌드됩니다.

디버그 빌드는 `applicationId` 에 `.debug` 접미사가 붙어 릴리즈 앱과 함께 설치할 수 있습니다.

---

## 로드맵

- **살까 말까** — 사기 전에 "지금 사면 하루 권장이 얼마로 바뀌는지" 미리 보여주는 구매 결정 도우미
- **소비 보류함** — 고민되는 지출을 30일 보류하고, 안 산 만큼을 "아낀 돈"으로 집계
- **홈 위젯** — 홈 화면에서 오늘 권장 금액 확인
- **알림** — 보류 만료·결산 리포트 안내
- **리포트 확장** — 주간 리포트, 연말 결산

---

## 참고

- 데이터는 서버 없이 기기 내부에만 저장됩니다. 기기 변경이나 앱 삭제에 대비해 **백업 기능 사용을 권장**합니다.
- 무료이며 광고가 없습니다.
- 1인 개발로 꾸준히 개선하고 있습니다.

## 개발 노트

예산 기간·금액 이월·금고 계산은 서로 얽혀 있어, 기능을 추가할 때 **anchorMonth 유일성**과 **금고 불변식**을 먼저 확인해야 합니다. DB 스키마나 백업 포맷을 바꿀 때는 과거 백업 호환성과 `BackupDao`·`BackupRepositoryImpl` 양쪽 반영이 필요합니다.

프로젝트 규칙과 구현 결정 이력은 [CLAUDE.md](CLAUDE.md), 다음 버전 기능 설계는 [docs/v1.1-design.md](docs/v1.1-design.md) 를 참고하세요.
