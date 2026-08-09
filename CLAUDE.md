# DayDone (데이던) — 프로젝트 설정

DayDone은 복잡한 가계부가 아니라, 월 수입에서 저축·고정비·미래 지출 준비금을 먼저 제외한 뒤 **"오늘 얼마까지 써도 되는지(오늘 권장 금액)"**를 보여주는 **1인 생활비 앱**입니다.

> 미래에 나갈 돈을 미리 생활비에서 빼두고, 남은 돈만 마음 편히 쓰게 한다.

DayDone은 소비를 **분석**하는 앱이 아니라, 오늘 쓸 돈을 **지켜주는** 앱입니다.

> 용어: 예전의 "오늘의 방어선"은 일상어로 어색해 **"오늘 권장 금액"**으로 통일한다. 넘겨도 재분배되므로 상한이 아니라 **권장**이라는 뉘앙스를 유지한다. (코드 식별자 `CalculateTodayDefenseLineUseCase` 등은 그대로)

---

## 1. 기본 정보

- 서비스명: DayDone / 데이던
- 플랫폼: Android
- 언어: Kotlin
- UI: Jetpack Compose
- 구조: MVVM + Room + DataStore + Hilt + Coroutines/Flow + Navigation Compose
- 날짜 계산: `java.time`
- 패키지: `com.jsworld.android.daydone`

---

## 2. 타깃 유저

사회초년생 / 직장인 / 1인 가구. 월급은 들어왔지만 실제로 얼마를 써도 되는지 불안한 사람, 비정기 지출(자동차세·재산세·보험료)로 예산이 흔들리는 사람, 복잡한 입력은 부담스럽지만 오늘의 생활비 기준선은 알고 싶은 사람.

---

## 3. 예산 기준 기간

**월급일**과 **예산 기준 기간**을 분리한다.

- 기본값: 매월 1일 ~ 말일
- 사용자는 **예산 시작일만** 선택한다. 종료일은 다음 예산 시작일 전날로 자동 계산.
  - 시작일 1일 → 1일~말일 / 시작일 25일 → 25일~다음 달 24일
- 시작일·종료일을 각각 선택하게 하지 말 것 (기간이 비거나 겹칠 수 있음).

관련 UseCase: `GetCurrentBudgetPeriodUseCase(today, budgetStartDay)`, `GetBudgetPeriodForMonthUseCase(yearMonth, budgetStartDay)`.

### 예산 시작일 커스텀 정책 (결정)

- **온보딩에서 먼저 정한다** (첫 실행). 데이터가 없을 때 정하므로 재분배 문제가 안 생김.
- 변경은 설정 탭에서. **전역 재계산 방식**: `budgetStartDay`는 단일 전역값이라, 바꾸면 과거·현재 모든 기간이 새 규칙으로 다시 나뉜다. "시작일 기준으로 기간이 다시 나뉜다"는 **안내 문구** 필수.
  - 예: 1일→24일로 바꾸면 7/13 지출은 `6/24~7/23`(=앱상 "6월") 기간으로 이동. 그래서 온보딩에서 먼저 받는 게 핵심.
- 범위 **1~31 (v1.1부터)**: 달에 없는 날짜(29~31)는 **그 달의 말일로 clamp** — 31 = "매월 말일" (설정 탭에 "매월 말일"로 표시). clamp는 두 기간 UseCase에 원래 들어있었고, 입력 검증(온보딩·설정·오늘 탭 예산 시트)과 엑셀 예산월 계산(`anchorMonthOf`)까지 동일 규칙 적용. 월당 기간 1개(anchorMonth 유일성)는 clamp에서도 성립 — 테스트로 고정(2월 포함 연속성 검증). `payday`는 계산 미사용(정보용).
- ⚠️ **불변식(load-bearing)**: 단일 전역 startDay → "**월당 기간 1개**" → `anchorMonth("YYYY-MM")`가 기간의 유일 키. 월 예산·고정비 월별 금액 오버라이드가 모두 이 키에 의존한다. "다음 기간부터 적용 / 발효월별 startDay / 브리지(이음) 기간"을 도입하면 한 달에 기간이 2개 생겨 이 키가 깨지므로, **키를 시작일자 기반으로 재설계**해야 한다. (그래서 v1은 전역 방식 유지)

---

## 4. 예정 차감 (v1.0 핵심 계산)

이번 예산 기간 안에 빠져나갈 **예정인** 저축·고정비를, 실제 출금 전이라도 미리 생활비에서 제외한다. (실제 출금일 전에 생활비로 착각하지 않도록)

저축/고정비 항목: 항목명 · 금액 · 유형(저축/고정비) · 반복(매월) · 출금일 · 시작월 · 종료월(선택) · 메모.
v1.0에서 실제 출금 완료 체크는 제외(v1.1+).

### 금액의 월별 이월 (구현됨, §8과 동일 패턴)

고정비/저축은 단일 레코드지만 **금액은 월별로 이월(carry-forward)** 한다.
- 저장: Room `ScheduledDeductionAmountEntity(deductionId, anchorMonth PK, amount)` 오버라이드
- 조회: `ResolveScheduledDeductionAmountsUseCase` — anchorMonth 이하 최근 오버라이드, 없으면 레코드의 최초 amount
- 편집: **금액**은 그 기간(anchorMonth)부터 이월 적용(과거 보존), **제목·유형·출금일**은 항목 전체에 반영(base amount는 안 건드림). 삭제 시 오버라이드도 함께 제거.

### 항목 "종료" (구현됨)

편집 시트에 **"이번 달까지만 하고 종료"**(확인 다이얼로그) 제공: `endYearMonth = 보고 있는 기간의 anchorMonth`로 설정(`EndScheduledDeductionUseCase`) → 그 달까지 반영, 다음 기간부터 예정 차감에서 제외, 지난 기록 보존. **삭제**(전체 제거, 확인 다이얼로그)와 구분됨. 오늘·월 탭 공통.

---

## 5. 미래 지출 / 준비 항목

세금·보험만이 아니라 **미리 돈을 모아둘 모든 것**을 하나의 "준비 항목"으로 다룬다. 사용자가 직접 추가할 수 있다(예: 자동차세·재산세·자동차 보험료, **엄마 생신 선물·가족 여행**).

항목: 항목명 · 카테고리(세금/보험/기념일/기타) · 총 금액 · 목표월(납부/이벤트월) · 준비 시작월 · 반복(1회/매년) · 메모.
> 금고는 "돈을 모으는" 곳이라 준비 항목엔 **목표 금액이 있다.** 금액 없는 순수 알림(리마인더)은 v1 범위 밖.

고정비와 달리 **자동 차감하지 않고**, 사용자가 **"준비하기"**를 눌렀을 때 그 달 지출로 처리한다.

> **프레이밍 (결정):** 앱은 돈을 보관하지 않는다. 금고는 "언제부터 얼마씩 **따로 통장에 옮겨두면** 좋을지" 안내하는 도우미다. "준비하기"는 실제로 딴 통장에 옮긴 것을 **기록**하는 행위 → 그만큼 생활비에서 실제로 빠지므로 차감 로직은 그대로 정합(계좌 분리는 권장이지 강제 아님). UI 문구는 "옮겨두기/옮겨뒀어요"로 정직하게 표기.

**준비금은 `ExpenseEntity(type=FUTURE_PREPARE, futureExpenseId=…)` 지출 1줄이 유일 진실원**(생활비 차감은 여기서 한 번만 일어남). 준비하기 시: ① 그 달 FUTURE_PREPARE 지출 생성 → ② 순수 생활비 자동 차감 → ③ 금고 "준비됨"은 이 지출들의 합으로 **파생**.

이번 달 준비 제안액 = (총액 − 이미 준비된 금액) ÷ (목표월까지 남은 개월).

---

## 6. 금고

준비 항목을 **한데 모아 보여주고 준비 진행을 시각화**하는 탭. 앱이 자동 저금하는 게 아니라 사용자가 "준비하기"로 빼둔 돈이 쌓여 보이는 공간.

- **금고 값은 파생**: 항목별 "준비됨" = 그 항목의 FUTURE_PREPARE 지출 합(현재 사이클). **입금을 별도 테이블에 이중 기록하지 않는다**(이중계산 방지).
- 구성: ① 기능 안내(빈 상태 설명) ② 이번 달 준비 제안(모아 보기 + 총액) ③ 항목별 카드(목표/준비됨/남은/목표월, 진행 바) ④ 월별 준비 내역 ⑤ 납부 완료 내역 ⑥ 준비 항목 추가.
- **납부 완료**(목표월): 이미 준비된 만큼은 생활비 **재차감 안 함**, **부족분만** 그 달 지출로 추가. `FutureExpense.lastPaidYearMonth`로 사이클 종료 기록. 반복(매년)이면 다음 해 사이클로 리셋(그 항목 금고 0부터).
- 🔒 **불변식**: `한 항목이 생활비에서 뺀 총액 = totalAmount` = (준비금 합) + (납부월 부족분). 초과/누락 0.
- 안내 문구는 §13 톤: "큰 지출을 미리 조금씩 빼두면 그 달이 편해져요."

---

## 7. 오늘 권장 금액 계산

> 오늘 권장 금액 = 남은 순수 생활비 ÷ 남은 일수

남은 순수 생활비 = 월 수입 − 예정 차감 저축 − 예정 차감 고정비 − 미래 지출 준비금 − 일반 지출.

초과 시 남은 일수 기준으로 자동 재분배한다(권장 금액을 매일 `남은예산 ÷ 남은일수`로 다시 계산하므로 자연히 재분배됨). 초과를 **실패로 표현하지 말고 조정 중심**으로 안내.

> ⚠️ 현재 `TodayViewModel.loadToday`에서 **`futurePrepareAmount = 0L`로 하드코딩**되어 있다(미래 지출 미구현). 미래 지출/준비하기를 붙일 때 이 값을 실제 준비금과 연결해야 하며, 준비금을 `ExpenseEntity(type=FUTURE_PREPARE)`로도 저장한다면 **일반 지출 합계와 이중 차감되지 않도록** 저장/차감 경로를 하나로 정해야 한다.

---

## 8. 월별 예산 모델 (per-month budget)

> ⚠️ 이 절은 실제 구현에 맞춰 갱신됨. §13 데이터 모델과 함께 본다.

월 예산 금액은 **월마다 다를 수 있다** (예: 7월 3,877,000 → 8월 3,677,000). 따라서 단일 전역값이 아니라 **월별 레코드 + 직전값 이월(carry-forward)**로 관리한다.

- 저장: Room `MonthlyBudgetEntity(anchorMonth "YYYY-MM" PK, income)`
- 조회: 대상 월 이하에서 가장 최근 레코드 사용 → 없으면 `BudgetProfile`의 기본 수입으로 폴백 (`ObserveEffectiveMonthlyBudgetUseCase`)
- 편집: 해당 달 레코드만 저장 (`SetMonthlyBudgetUseCase`), 과거 달 값은 보존
- **anchorMonth = 예산 기간 시작일(period.startDate)의 YearMonth**
- `BudgetProfile`(DataStore)은 이제 설정성 값만 담당: 월급일 · 예산 시작일 · 기본 수입(폴백용)

동작 예: 7월 3,877,000 저장 → 8월 진입 시 7월값 이월 표시 → 8월을 3,677,000으로 수정하면 8월 레코드 생성(7월 보존) → 9월은 다시 8월값 이월.

---

## 9. 화면 구성

**하단 탭바(구현됨)**: 오늘 · 월 · 금고 · 설정 4탭 + **하단 중앙 `+`(공용 빠른 입력)**. `+` → "무엇을 추가할까요?"(지출/수익/저축·고정비/이번 달 예산) → 오늘 탭으로 이동 후 해당 입력 시트 오픈. 각 탭의 항목은 **탭하면 수정/삭제 시트**(오늘·월 공통, 입력 시트는 `internal`로 재사용). 요약 카드(오늘 "이번 기간 요약" / 월 "예산 요약")는 **핵심 숫자 + "자세히" 접기** 패턴으로 통일.

### 오늘 탭 (핵심 화면 · 권장 금액 전용)

전체 캘린더를 넣지 않고 **오늘 권장 금액 + 날짜 칩 + 선택 날짜 내역**에 집중한다.

표시: 오늘 남은 권장 금액(라벨 "오늘 이만큼 남았어요") · 예산 진행 바 · 남은 순수 생활비 · 남은 일수 · 날짜 칩 가로 리스트 · 빠른 지출 버튼 · 선택 날짜 내역 · 예정 차감 요약 · 상태 메시지.

- 날짜 칩: 오늘 기준 앞뒤 며칠(총 5~7개)만. 지출/예정 차감 있는 날 표시.
- **상단 권장 금액 카드는 항상 오늘 기준 유지.** 날짜 칩을 눌러도 하단 내역만 선택 날짜로 바뀐다.
- 라벨: 과거 "7월 1일 내역" / 오늘 "오늘 내역" / 미래 "7월 3일 예정".

### 월 지출 탭 (= 코드상 `MonthlyScreen` / `MonthlyRoute`)

오늘 탭보다 넓은 범위에서 **월별 예산과 지출 흐름**을 보는 화면. 상단바 달력 아이콘으로 진입, 이전/다음 달 이동.

구성 (위→아래 층):
1. **월 이동 헤더** + 모드 칩 — 지난 달(결산) / 이번 달(진행 중) / 다가올 달(계획)
2. **예산 요약 카드** — 월 예산(§8, 이 달 예산 수정 가능) · 추가 수익 · 사용 가능 예산 · 저축/고정비 · 총 지출 · 남은 금액
3. **월 캘린더** — 예산 기간 전체를 주 단위(일~토) 그리드로. 각 날짜칸에 지출(•)/예정 차감(◦) 마커. 날짜 탭 → 선택 날짜 상세.
4. **선택 날짜 내역** — 그날의 지출/추가수익/예정 차감

> 캘린더 원칙:
> - 캘린더는 **지출 흐름**을 위한 것. 예산(스칼라)은 상단 요약으로 유지하고 날짜칸에 예산을 뿌리지 않는다.
> - 캘린더 날짜 탭은 그날 **내역만** 보여준다. **권장 금액은 계산/표시하지 않는다** (권장 금액은 오늘 탭 전용).
> - **오늘 탭에는 캘린더를 넣지 않는다** (날짜 칩 유지).

### 금고 탭

전체 준비 금액 · 금고별 카드 · 월별 준비 내역 · 납부 완료 내역.

### 설정 탭

월 수입 · 월급일 · 예산 기준 기간 · 저축 관리 · 고정비 관리 · 미래 지출 관리 · 빠른 지출 버튼 · 데이터 초기화 · 앱 정보.

---

## 10. v1.0 범위

월 수입/월급일/예산 기간 설정, 저축·고정비 등록 및 출금일·종료 예약, 예정 차감, 미래 지출 등록·준비 안내·준비하기·준비금 지출·금고 반영·납부 완료, 오늘 권장 금액, 오늘 탭 날짜 칩, 일반 지출 입력, 자동 재분배, 빠른 지출 버튼, **월별 예산(월 지출 탭에서 월 이동/수정)**.

### v1.0 제외 (필요 시 v1.1+로)

은행/카드 자동 연동, 영수증 OCR, 복잡한 카테고리 통계, 자산 총액/투자/대출 관리, 커플·공동 가계부, 로그인 동기화, AI 소비 분석, PDF 리포트, 고정비 실제 출금 완료 체크.

---

## 11. 개발 방향

**Jetpack Compose 우선.** 카드형 UI, 상태 기반 화면, 날짜 칩/캘린더, 바텀시트 입력 중심. XML은 기존 XML 프로젝트 유지가 필요할 때만 대안으로 설명.

계산 로직은 ViewModel에 몰아넣지 말고 **UseCase로 분리**. 흐름: Room → Repository → UseCase → ViewModel → UiState.

권장/현존 UseCase:
- `GetCurrentBudgetPeriodUseCase`, `GetBudgetPeriodForMonthUseCase`
- `GetScheduledDeductionsInPeriodUseCase`, `CalculateTodayDefenseLineUseCase`
- `ObserveEffectiveMonthlyBudgetUseCase`, `SetMonthlyBudgetUseCase`
- `ObserveScheduledDeductionAmountsUseCase`, `ResolveScheduledDeductionAmountsUseCase`, `SetScheduledDeductionAmountUseCase` (고정비 금액 월별 이월)
- `AddExpenseUseCase` / `UpdateExpenseUseCase` / `DeleteExpenseUseCase` (추가수익·고정비도 동일 CRUD)
- (예정) `GetFutureExpensePrepareSuggestionsUseCase`, `PrepareFutureExpenseUseCase`, `CompleteFutureExpensePaymentUseCase`, `GetMonthlySummaryUseCase`

---

## 12. 데이터 모델

- `BudgetProfileEntity`(DataStore): 월급일 · 예산 시작일 · 기본 수입(폴백)
- `MonthlyBudgetEntity`: anchorMonth("YYYY-MM") · income — **월별 예산 레코드, 이월 조회**
- `FixedExpenseEntity`(= `ScheduledDeduction`): 저축/고정비 type · 금액 · 출금일 · 시작월 · 종료월 (저축·고정비 모두 예정 차감이라 한 테이블 type 구분)
- `ScheduledDeductionAmountEntity`: deductionId · anchorMonth("YYYY-MM") · amount — **고정비/저축 금액의 월별 이월 오버라이드** (§4)
- `FutureExpenseEntity`: title · category(세금/보험/기념일/기타) · totalAmount · targetYearMonth · prepareStartYearMonth · repeat(ONCE/YEARLY) · memo · lastPaidYearMonth — **준비 항목**
- `ExpenseEntity`: 일반 지출 + **준비금 지출**(type=FUTURE_PREPARE, futureExpenseId로 준비 항목 연결) — 준비금 차감의 **유일 진실원**
- `ExtraIncomeEntity`: 추가 수익
- ~~`VaultTransactionEntity`~~: **도입 안 함** — 금고는 FUTURE_PREPARE 지출 합으로 파생(이중계산 방지). 필요 시 v1.1 표시용으로만.

Room 마이그레이션: 스키마 변경 시 정식 `Migration` 제공(데이터 보존) + `fallbackToDestructiveMigration` 병행. 현재 **DB version 6** (1→2 monthly_budgets, 2→3 scheduled_deduction_amounts, 3→4 future_expenses, 4→5 expenses.isEssential, 5→6 no_spend_records).

### 이번 기간 리포트 (구현됨) — 진행 중 전용

- 진입: 월 탭 요약 카드 아래 **"이번 기간 리포트 보기"**(mode==CURRENT일 때만) → 별도 화면(`ReportRoute`). **모든 값은 계산 시점 파생, 저장 없음**(`BuildMonthlyReportUseCase`).
- 구성: ① 페이스 카드(기간 경과율 vs 생활비 소진율, ±7%p로 GOOD/ON_TRACK/FAST, §13 톤 헤드라인 + 마커 진행 바) ② 미니 지표(하루 평균·무지출 일수·필수 비중) — **무지출은 "전체 기록의 최초 지출 날짜"부터 집계**(`GetFirstExpenseDateUseCase`, DB `MIN(date)` 파생 — DataStore firstUseDate는 복원·재설치에 흔들려서 기본으로 안 씀). **예외**: 가입일 전 지출 날짜가 기간 시작일 하나뿐인 패턴(= "이전 지출" 한 줄 유저)은 가입일부터 집계 — 그 한 줄 때문에 가입 전 날들이 가짜 무지출로 부풀지 않게. 집계 시작이 기간 중간이면 `trackingStartDate`로 노출 → 미니 지표 아래 "🌱 {날짜}부터 기록을 시작했어요…" 안내(첫 기간에만 뜨고 이후 기간은 자동 소멸) ③ **자주 쓴 곳**(카테고리 합산 TOP5, 탭하면 같은 지출명 세부 펼침) ④ 남은 기간 가이드(예상 잔액·하루 권장, 남으면 금고 제안) ⑤ 고정지출 요약(수입 대비 %).
- **카테고리는 내장 키워드 사전으로 파생**(`ClassifyExpenseCategoryUseCase`, 16종 `ExpenseCategory`): 입력엔 카테고리 없음(마찰 0), 지출명 소문자·공백제거 후 포함 매칭. **가장 긴 키워드 승리**(아이스크림(5)>크림(2)→카페 — 짧은 키워드 가로채기 방지), 동률이면 선언 순서(구체적 카테고리 먼저 — "생일 선물"=경조사). 한 글자 키워드(술/회/침/팩/차/펌/약/책)는 **정확 일치만**(EXACT_ONLY). 키워드는 **공백 없이·영문 소문자로** 등록. 미매칭=기타.
- **지난 기간 대비 (구현됨, v1.1)**: `MonthlyReport.previous`(`PreviousComparison`) — **전부 "같은 시점(dayIndex)까지"끼리 비교**(기간 전체와 비교하면 진행 중엔 항상 "덜 씀" 왜곡). 표시 2곳: ① 페이스 카드 아래 한 줄 "지난 기간 이맘때보다 N원 덜/더 쓰고 있어요"(결산은 "지난 기간보다 ~썼어요", 덜 쓸 때만 성공 색) ② 미니 지표(하루 평균·무지출)에 "지난 기간 {값}" 보조 줄. **숨김 조건**: 지난 기간이 없거나 부분 기록(최초 지출 > 지난 기간 시작일)이면 previous=null — 어긋난 비교보다 없는 게 §13. 무지출 비교는 같은 확정 일수(진행 중=dayIndex-1일, 결산=dayIndex일)로 창을 맞춤. ViewModel이 지난 기간 지출을 함께 관찰(중첩 combine).
- **결산 리포트 (구현됨)**: 같은 화면이 겸용 — `report?month=yyyy-MM` 인자로 과거 기간 진입 시 `MonthlyReport.isFinal=true`(기간 끝 기준 계산: dayIndex=totalDays, remainingDays=0, projectedLeftover=실제 잔액). UI는 결산 문구(과거형 헤드라인, "OO원을 지켜냈어요. Day Done! 🎉" / 초과 시 §13 톤)로 분기. 진입: ① 월 탭 지난 달 → "기간 결산 리포트" 카드 ② 오늘 탭 — **새 기간 첫 3일간** "지난 기간 결산 리포트가 나왔어요" 배너.
- 고정지출 **상세 분석**(항목별 수입 대비 비중 + 저축률 평가 + 맞춤 제안 규칙 13종)은 블러 잠금 → 현재는 탭 해제, **애드몹 연동 후 리워드 시청 콜백으로 교체**(코드 TODO 표시).
- 월 탭 캘린더: 무지출 챌린지 창 안의 **성공한 날 초록 체크 마커** + 범례 (지난 날만, `isSuccessDay` 재사용).

### 무지출 챌린지 (구현됨) — 예산 기간과 독립

- **예산 기간과 무관**하다. 유저가 **도전 일수**를 정하고 "오늘부터 시작" → `startDate`부터 `targetDays`일 창으로 진행. (설정 탭에서 시작/그만두기)
- 설정(DataStore, `NoSpendChallengeDataSource`): enabled · mode(FULL/ESSENTIAL_ALLOWED/CAP) · capAmount · targetDays · **startDate(epochDay, null=미시작)**.
- 성공 판정(`EvaluateNoSpendProgressUseCase`): 창 `[startDate, startDate+targetDays-1]` 기준. **일반 지출(GENERAL)만** 본다 — 준비금(FUTURE_PREPARE)·저축/고정비는 무지출을 깨지 않음. FULL=지출 0건 / ESSENTIAL_ALLOWED=`isEssential` 표시 외 0건 / CAP=**필수 제외** 합계 ≤ 상한.
- `ExpenseEntity.isEssential`(v5): 지출 입력/수정 시트의 "필수 지출" 체크. **챌린지가 시작돼 있고 mode가 ESSENTIAL_ALLOWED/CAP일 때만** 체크박스 노출(오늘·월 공통, `showEssentialCheckbox`).
- 관찰: 챌린지는 예산 기간과 독립이라 **TodayViewModel에서 별도 flow**(`observeChallenge`, 창 날짜로 지출 조회)로 처리. Monthly는 체크박스 노출 여부만 관찰.
- 표시: 오늘 탭 카드 — {N}일째·성공 {M}/{목표}일, 진행 바, 🔥연속, 상태 문구(§13 톤). 창이 지나면 완료 카드(🎉) + **[그만 보기 / 새로 도전하기]** 버튼(그만 보기=enabled off·설정 유지, 새로 도전=같은 설정으로 오늘부터). 성공 일수는 지난 날만 확정, 오늘은 진행 중으로 별도.
- **도전 기록(v6)**: 창이 끝나면 `no_spend_records`(startDate PK — 중복 저장 방지)에 자동 저장. 설정 탭 → "도전 기록"에서 별도 화면(`ChallengeHistoryRoute`)으로 모아보기(기간·성공/도전 일수·모드·진행 바·배지 🏆/🔥/🌱).

---

## 13. UX 문구

압박하지 않고 **안정감**을 주는 방향.

좋은 예: "오늘은 이 금액 안에서 쓰면 괜찮아요." / "적금은 이미 생활비에서 제외해두었어요." / "자동차세 낼 돈을 미리 준비해볼까요?" / "오늘 조금 넘겨도 괜찮아요. 남은 날에 다시 나눠볼게요." / "오늘의 돈을 지켰어요. Day Done."

피할 예: 예산 실패 / 과소비 경고 / 위험합니다 / 소비를 줄이세요 / 돈을 너무 많이 썼어요.

---

### 날짜 경계 · 테마 · 테스트 (구현됨)

- **자정 갱신**: `TodayViewModel.todayFlow`(MutableStateFlow<LocalDate>)가 "오늘"의 단일 출처. 화면 복귀 시 `onResumed()`가 날짜 변경을 감지해 flow를 갱신 → 기간·권장 금액·챌린지가 새 날짜로 재계산된다(선택 날짜도 어제의 '오늘'이었다면 함께 이동). Monthly도 `onResumed()`로 오늘 마커·모드 재계산. Route에서 `LifecycleEventEffect(ON_RESUME)`로 연결. **화면 코드에서 `LocalDate.now()`를 직접 쓰지 말고 이 flow를 쓸 것.**
- **강조 색상**: 컬러스킴에 없는 성공/초록 계열은 `ui/theme/AccentColors.kt`의 `DayDoneAccent`(successText·successContainer·onSuccessContainer·noSpendCheck)에서만 관리. 라이트/다크가 함께 정의돼 있으니 **화면에서 `Color(0xFF…)` 하드코딩 금지**.
- **콘텐츠 색상**: 앱 루트(`DayDoneRoot`)를 `Surface(color = background)`로 감싸 `LocalContentColor`를 내려준다. 이게 없으면 Material 기본값(**검정**)이 쓰여 다크 모드에서 툴바 제목·섹션 헤더·화살표 아이콘이 안 보인다. 새 전체화면을 만들 때도 `Surface`로 감싸거나 색을 명시할 것(`DayDoneTopBar`는 자체적으로 `onSurface` 명시).
- **유닛 테스트**(`app/src/test`, junit4, 51개): 기간 계산·권장 금액·금액 이월·무지출 판정·카테고리 분류·리포트 페이스. 계산 로직을 바꾸면 `./gradlew :app:testDebugUnitTest`로 먼저 확인한다. 회귀 방지용으로 남긴 케이스: 초과 5,400원(퍼센트 절삭), 아이스크림/아이크림(긴 키워드 승리), 8월 수정 시 7월 보존(이월).

## 14. 응답 기준

1. 1인 개발 MVP 관점, 기능을 과하게 키우지 않는다.
2. Android / Kotlin / Compose 기준.
3. 계산 로직은 UseCase로 분리.
4. Room → Repository → UseCase → ViewModel → UiState 흐름.
5. 날짜는 `java.time`.
6. 오늘 탭은 "오늘 권장 금액" 중심 유지.
7. 전체 캘린더는 월 지출 탭, 오늘 탭은 날짜 칩.
8. 본질을 "가계부"보다 **"오늘 쓸 돈을 알려주는 생활비 앱"**으로 유지.

---

## 15. 구현 결정 & 리스크 레지스터 (앞으로 만들 때 주의)

미구현 기능이 기존 구조(기간·이월·anchorMonth)와 부딪히는 지점. 우선순위 순.

**🔴 미래 지출 / 준비하기 / 금고 — 돈 이중계산 (설계 확정됨, 구현 시 준수)**
- 해결책: **준비금 = `ExpenseEntity(FUTURE_PREPARE)` 1줄이 유일 진실원**, 금고는 그 합으로 **파생**(별도 입금 테이블 없음). 납부월엔 재차감 없이 **부족분만** 추가. (§5~6)
- 남은 위험(구현 시 테스트): 부분 준비, 총액 변경, 준비 항목 삭제 시 FUTURE_PREPARE 지출 롤백, 매년 반복 사이클 경계(lastPaidYearMonth 기준 현재 사이클 산정).
- §7의 `futurePrepareAmount=0`은 준비금이 지출로 잡히므로 **그대로 0 유지 가능**(이중차감 없음). 단 준비금을 일반 지출 합에 반드시 포함시킬 것.

**✅ 고정비/저축 "종료" (구현됨, §4)**

**🟡 anchorMonth 유일성 = 뼈대 가정**
- 월 예산·고정비 금액 오버라이드가 "월당 기간 1개(anchorMonth 유일)"를 전제. startDay 전역 방식 덕에 성립. 불규칙 기간 도입 시 키를 시작일자 기반으로 재설계해야 함. (§3 불변식)

**✅ 백업/복원 (구현됨)**
- 설정 탭 → "데이터 백업(다운로드 폴더에 저장)" / "백업 파일에서 복원". 파일명 `daydone-backup-YYYYMMDD.json`.
- 저장 위치: **MediaStore 로 `Download/DayDone/백업/` 에 바로 저장**(Android 10+ 권한 불필요). 실패하거나 Android 9 이면 SAF `CreateDocument` 로 자동 대체.
- **복원 (v1.0.1)**: 복원 행 → **앱 내 파일 목록 시트**(`ListBackupFilesUseCase`, MediaStore 최신순, `Download/DayDone/%` + `.json`) → 파일 탭 → 확인 다이얼로그 → `ImportBackupFromFileUseCase`. ⚠️ MediaStore 는 **이 앱이 만든 파일만** 보여줘서 재설치·기기 변경 후엔 목록이 빈다 — 시트 빈 상태에 "목록엔 안 보여도 파일은 백업 폴더에 그대로 있다" 안내 + **"폴더에서 직접 선택"** 버튼(SAF `OpenDocument`, `OpenBackupDocument` 계약이 `EXTRA_INITIAL_URI`로 백업 폴더에서 열어줌; 폴더 경로 바꾸면 함께 수정). 읽기 권한 추가는 금지("권한 없음" 포지션).
- `BackupRepository`(+`BackupDao`: 전 테이블 일괄 read/insert)가 **id 를 그대로 보존**해 항목 간 연결(`futureExpenseId`, `deductionId`)이 유지된다. 설정(수입·시작일·firstUseDate·온보딩 플래그·챌린지)도 함께 저장.
- 가져오기는 **전체 대체**: JSON 을 먼저 전부 파싱·검증한 뒤 `clearAllTables()` → 삽입(중간 실패로 데이터가 비는 것 방지). 확인 다이얼로그 필수.
- **복원 후 상태 보정 (v1.0.1)**: ① `preJoinSpendHandled`는 백업 값과 무관하게 **무조건 처리됨**으로 — 복원 유저는 기록을 가져온 상태라 "가입 전 지출" 배너 대상이 아님 ② `firstUseDate`는 **백업 그대로 충실 복원** — 값이 있으면 그 값, **없으면(온보딩 기능 이전 레거시 설치) `clearFirstUseDate()`로 비운다**(null=제한 없음이 원래 상태). 재설치 온보딩이 찍은 "오늘"을 남겨두면 리포트 무지출 일수가 그날부터 세어지는(1일 증상) 버그가 있었음. ⚠️ min(백업, 최초 기록) 같은 "보정" 시도 금지 — 가입 전 지출(기간 시작일에 한 줄)을 쓴 유저가 복원하면 가입 전 가짜 무지출이 부활한다. 복원의 원칙은 보정이 아니라 **재설치 전 상태 재현**.
- 새 테이블/컬럼을 추가하면 **`BackupDao`·`BackupRepositoryImpl` 양쪽에 필드를 반영**할 것. 포맷이 바뀌면 `BACKUP_VERSION` 을 올린다.
- `allowBackup=false`(기기 교체 시 소실)를 이 기능이 보완한다.
- **엑셀 내보내기 (v1.0.1)**: 설정 → "지출 내역 내보내기 (엑셀)" — **xlsx 1개 파일, 시트 2개** 저장(`Download/DayDone/엑셀/daydone-내역-YYYYMMDD.xlsx`, `ExportExcelUseCase`): ① **지출 시트** — 예산월별 구역("2026년 7월 지출 내역" 굵은 제목 + 헤더 `예산월,날짜,구분,이름,금액,필수 지출,메모` + 행들 + 빈 줄). 예산월은 달력 월이 아니라 **예산 기간의 anchorMonth**(월 탭과 일치). 지출·준비금(구분 "준비금")은 음수, 추가수익은 양수 ② **고정지출 시트** — 저축/고정비 목록(금액은 최신 월별 오버라이드, 없으면 최초 금액). xlsx는 라이브러리 없이 `data/excel/XlsxWriter`(xlsx=XML ZIP, inlineStr 방식, 굵게 s="1")로 직접 생성 — POI 등 무거운 의존성 금지. `XlsxWriterTest`가 ZIP/XML 구조 검증(openpyxl로도 열림 확인). 복원용 아님(가져오기는 JSON 백업만).

**✅ 개인정보 강조 문구 (v1.0.1)**
- 포지션: "서버가 없어서 유출될 곳 자체가 없어요" — **구조적 사실**만 말하고 타사 유출 언급·공포 마케팅 금지(§13 톤).
- 위치: ① 온보딩 하단 🔒 한 줄 ② 설정 탭 백업 카드 아래 🔒 안내. 스토어 자세한 설명에도 동일 포지션 반영됨.

**✅ 데이터 초기화 (구현됨)**
- 설정 탭에서 확인 다이얼로그 후 `DataResetRepository.resetAll()` = Room **`clearAllTables()`**(전 테이블 자동, 새 테이블 추가돼도 누락 없음) + DataStore clear. 초기화하면 온보딩 플래그도 지워져 온보딩부터 재시작.

**✅ 가입 전 지출 (구현됨, v1.0.1)**
- 기간 **중간에** 온보딩한 유저는 그 기간의 기존 지출이 0으로 잡혀 권장 금액이 부풀어 보임 → 오늘 탭 배너 "{시작일}부터 오늘까지 쓴 돈이 있다면" → 다이얼로그에서 **총액 한 줄**만 입력(항목별 입력 금지 — 가계부 노동).
- 저장 방식: `AddExpenseUseCase(title="이전 지출", date=**period.startDate**)` — 오늘 날짜로 넣으면 오늘 지출로 잡혀 권장 금액이 즉시 초과되므로 반드시 기간 시작일. 일반 지출이라 수정/삭제/백업/리포트 모두 기존 로직 그대로.
- 표시 조건: `!preJoinSpendHandled && firstUseDate ∈ 현재 기간 && period.start < firstUseDate && 가입 후 3일 이내`(늦은 노출 방지). 저장/건너뛰기 시 `preJoinSpendHandled`(DataStore) on — 다시 묻지 않음. 다이얼로그 바깥 탭은 플래그 유지(배너 잔존). 다이얼로그에 "월 탭 캘린더의 {시작일} 내역에서 고칠 수 있다" 안내 포함.
- 리포트 무지출 일수는 `max(period.start, 전체 기록의 최초 지출 날짜)`부터 집계(기록 시작 전 가짜 무지출 방지 — firstUseDate 대신 데이터 파생, §12 리포트 참고). 챌린지는 유저가 시작일을 정하므로 clamp 안 함.

**✅ 온보딩/설정 (구현됨)**
- 온보딩: `onboarding_done` 플래그(DataStore)로 첫 실행 게이팅. 월 수입·예산 시작일을 먼저 받음(`CompleteOnboardingUseCase`). **온보딩 완료일(`firstUseDate`)도 저장** → 그 날이 속한 기간의 달이 **탐색 하한**: 월 탭은 그 이전 달로 못 가고(◀ 비활성), 오늘 탭 결산 배너도 시작 달 이전 기간이면 안 뜸(가짜 "전액 지켜냄" 결산 방지).
- 설정 탭: 월 수입(기본값)·예산 시작일 수정(전역 재계산 경고 문구 포함)·데이터 초기화. `payday`는 계산 미사용이라 **노출 안 함**.
- 남은 주의: 날짜 `LocalDate.now()`(로컬) vs DatePicker(UTC) 혼용 → 알림/자동처리 붙이면 타임존 통일. startDay "말일" 요구 시 clamp를 두 기간 UseCase에 일관 적용.
