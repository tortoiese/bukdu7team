# PROMPTS.md — Claude Code 프롬프트 세트

각 프롬프트는 **새 세션 또는 `/clear` 후** 하나씩 붙여넣는다. 한 프롬프트에 여러 단계를 합치지 않는다.
모든 프롬프트 앞에 붙는 전제: Claude Code는 `CLAUDE.md`를 자동으로 읽는다. 그래도 관련 문서를 명시적으로 지목해 주는 게 정확도가 높다.

---

## #0. 세션 시작용 고정 문구 (매번 맨 앞에)

```
먼저 CLAUDE.md, docs/DESIGN_SYSTEM.md, docs/API_CONTRACT.md 를 읽고 시작해.
이 프로젝트의 절대 규칙 R1~R8을 위반하는 코드는 작성하지 않는다.
작업 전에 무엇을 만들 계획인지 5줄 이내로 먼저 말하고, 내가 "진행"이라고 하면 코드를 작성해.
완료 후에는 변경한 파일 목록과 확인 방법만 간단히 보고해. 코드 전문을 다시 출력하지 마.
```

---

## #1. 스캐폴딩

```
[#0 문구]

ENTRY 프로젝트의 모노레포 스캐폴딩을 만들어.

web/ (React)
- Vite + React 19 + TypeScript strict
- Tailwind CSS v4 (@tailwindcss/vite 플러그인, @theme 방식)
- react-router v7 (createBrowserRouter), zustand, motion
- @fontsource/archivo, @fontsource/courier-prime, @fontsource/noto-sans-kr 설치 후 main.tsx에서 필요한 weight만 import
- src 구조: app/ screens/ components/ features/ brand/ styles/ types/ i18n/
- 환경변수 VITE_API_BASE, .env.example 포함
- eslint + prettier 설정, npm run build 통과

api/ (Spring Boot)
- Spring Boot 3.3.x, Java 21, Gradle Kotlin DSL
- 의존성: web, data-jpa, validation, springdoc-openapi-starter-webmvc-ui, h2, postgresql, lombok 없이 record 사용
- 패키지 io.entry, 도메인 기준 패키지 분리 (session, scan, passport, intent, transfer, advisor, admin, persona, ai, common)
- application.yml + application-local.yml(H2 file) + application-prod.yml(Postgres, server.port=${PORT:8080})
- common/ApiResponse<T> record, common/ApiError, @RestControllerAdvice 전역 예외 처리
- CorsConfig: entry.cors.origins 프로퍼티로 허용 오리진 주입
- GET /api/v1/health → { status, version, profile }

루트
- package.json에 concurrently로 "dev" 스크립트 (web dev + gradlew bootRun)
- README.md에 로컬 실행 3줄

마지막으로 web 첫 화면에 /api/v1/health 응답을 표시해서 연결을 확인할 수 있게 해.
```

---

## #2. 디자인 시스템

```
[#0 문구]

docs/DESIGN_SYSTEM.md 를 그대로 구현해. 문서에 없는 색, 폰트, 반경, 그림자를 새로 만들지 마.

1) web/src/styles/tokens.css
- 문서 2장의 컬러 변수, 4장의 간격 변수, 3장의 폰트 변수를 CSS 변수로 정의
- Tailwind v4 @theme 블록에 매핑 (예: --color-ink-700, --font-mono)
- 타입 스케일 6종을 유틸리티 클래스로 (.t-display-l, .t-label, .t-mrz ...)

2) web/src/components/ 공통 컴포넌트
- Button (primary/secondary, 높이 52, radius 0, mono 대문자 라벨)
- Field (mono 라벨 + Archivo 값, 2열 그리드 지원)
- HairlineSection (1px 구분선 섹션 래퍼, 카드/그림자 없음)
- MrzBar (하단 고정 2행, 값 변경 시 글자 단위 스크램블 240ms, 탭하면 접힘, aria-hidden + 별도 접근 가능 텍스트)
- Stamp (SVG, mix-blend-mode multiply, rotationSeed prop으로 각도 고정, 찍히는 모션 180ms)
- GuillocheBg (SVG pattern으로 여권 미세 패턴, opacity 0.04)
- StockStatus (좌측 4px 바 + 텍스트, IN_STOCK/TRANSFERABLE/ONLINE_ONLY/OUT_OF_STOCK)
- Toast (MRZ 밴드 위, mono 2행, 2.4초)
- Loading (스피너 금지, "PROCESSING<<<" 의 < 개수가 늘어나는 mono 텍스트)

3) web/src/components/MobileFrame.tsx
- 402px 기준 모바일 컨테이너, 좌우 20px 여백, 데스크톱에서는 중앙 정렬

4) /dev/kitchen-sink 라우트에 전 컴포넌트를 상태별로 나열

제약
- prefers-reduced-motion: reduce 에서 모든 변환 제거, 최종 상태 즉시 렌더
- 키보드 포커스 링 필수 (outline 2px var(--stamp))
- 컴포넌트 안에 색상 hex를 직접 쓰지 마. 전부 CSS 변수 참조
```

---

## #3. 백엔드 도메인 + 더미 데이터

```
[#0 문구]

docs/API_CONTRACT.md 의 1~4장, 10~11장을 구현해. AI는 아직 붙이지 않고, 의도 해석은 규칙 기반으로만 만들어.

엔티티
- AnonymousSession(id UUID, market, locale, entryPoint, createdAt) — PII 컬럼 절대 만들지 마
- ScanEvent(id, sessionId, productId, storeId, zoneId, scannedAt)
- SavedItem(id, sessionId, productId, savedAtStoreId, zoneId, savedAt)
- Passport(id, sessionId, passportNo, popupId, issuedPlace, issuedAt, accessTier)
- PassportStamp(id, passportId, zoneId, stampedAt, rotationSeed)

시드: api/src/main/resources/seed/ 에 products.json(20종), zones.json(4개: HERITAGE/MATERIAL/STYLING/PRODUCT), inventory.json(KR/HK/JP/US), personas.json(5종).
제품은 P+M, HHK, Sky Stream, Denim, M PUP, Lifestyle 라인으로 분산. 제품별 craftNotes 2~3개, 무게, 소재, 사이즈를 채워. 가격 필드는 만들지 마.
SeedRunner(ApplicationRunner)가 기동 시 비어있을 때만 주입.

규칙 기반 의도 해석 (intent/RuleIntentAnalyzer)
- 같은 line 내 3회 이상 스캔 + 사이즈 동일 → stage=SIZE_DECIDED, comparisonAxis=COLOR
- 같은 line 내 사이즈가 다름 → comparisonAxis=SIZE
- 서로 다른 line 교차 → stage=CATEGORY_COMPARE
- 재스캔(동일 productId 2회 이상) 발생 시 confidence +0.2
- 마지막 스캔 제품을 unresolved 후보로 지정
- rationale은 관측 사실만 서술 (예: "같은 라인 안에서 컬러만 바꿔 3회 스캔했습니다.")

InventoryPort 인터페이스 + DummyInventoryAdapter(seed/inventory.json 조회). 실제 옴니채널 API 연동 지점임을 주석으로 명시.

패스포트 어뷰징 방지: 동일 세션·동일 구역 재스탬프 무효, 스탬프 간 최소 60초, rotationSeed는 서버가 0~99 난수로 1회 발급 후 고정.

세션 헤더 X-Entry-Session 처리는 HandlerInterceptor로. 세션이 없으면 401 대신 새 세션 발급 후 meta.sessionRotated=true.

springdoc에 전 엔드포인트 노출. 끝나고 ./gradlew build 통과 확인.
```

---

## #4. P1 스캔 결과

```
[#0 문구]

P1 스캔 결과 화면을 만들어. 라우트 /s/:productId. docs/DESIGN_SYSTEM.md 규격 그대로.

화면 구성 (위에서 아래)
1) 캐릭터 인사 — HHK 캐릭터 말풍선 1문장. 지금은 서버가 주는 greeting 문자열 표시만
2) 제품명 + 라인명 (t-display-m), 그 아래 mono 라벨/값 2열 그리드로 소재·무게·사이즈. 사이즈는 현지 표기 + 원표기 병기
3) 제작 맥락 — craftNotes를 HairlineSection으로 나열. 이 영역이 화면에서 가장 넓어야 한다
4) 재고 — 이 매장 / 국내 타 매장 / 본국 시장 3단. StockStatus 컴포넌트 사용. 본국 시장 줄에 시장 코드를 mono로 강조
5) 액션 — "패스포트에 저장"(primary), "직원 호출"(secondary), "대화 이어가기"(text)
6) 하단 MrzBar

동작
- 진입 시 POST /scans 자동 호출 → intentSignal, greeting 수신
- 저장 시 POST /archive → Stamp 애니메이션 + Toast + MRZ의 SAVED 카운트 갱신
- 언어: 상단 우측에 시장/언어 전환 버튼. 누르면 PATCH /sessions/market 후 리페치
- 모든 문구는 i18n/ko.json, i18n/en.json 에서 읽는다. JSX에 한국어 하드코딩 금지 (en은 우선 ko와 동일 키만 채워두고 값은 영어로)

금지
- 가격, 할인, 별점, 리뷰, "추천", 위시리스트 하트 아이콘
- 로그인 유도 모달
```

---

## #5. P2 팝업 패스포트

```
[#0 문구]

P2 팝업 패스포트 화면을 만들어. 라우트 /passport.

상단 — 여권 데이터면
- 배경에 GuillocheBg
- passportNo(mono 대문자), 발급 장소, 발급일, 방문 구역 수, 저장 제품 수를 2열 라벨/값 그리드로
- 카드 테두리는 1px hairline, radius 0

중단 — 입국 기록
- 구역 4개를 2×2 그리드로. 방문한 구역은 Stamp(rotationSeed 사용), 미방문은 점선 사각형 + 구역 코드만
- 자물쇠/물음표 아이콘 금지

하단 — 열람 권한
- 현재 단계(1~4)와 다음 단계까지 남은 조건. 진행 게이지는 높이 2px
- 보상 문구는 접근 권한 표현만: 전용 시간대 우선 입장 / 팝업 한정 아카이브 열람 / 선공개 알림 / 다음 팝업 초대
- "할인", "쿠폰", "%" 문자가 들어가면 안 된다

그 아래 저장 목록 미리보기 3개 + "전체 보기"(→ /archive)

동작
- GET /passport. 없으면 "입구 태그를 스캔하면 발급됩니다" 빈 상태 + 개발용 발급 버튼(POST /passport)
- /z/:zoneId 라우트를 추가해 구역 QR 진입 시 POST /passport/stamps 후 /passport로 이동하며 스탬프 모션 재생
- 티어가 올라가면 Toast로 새 열람 권한 안내
```

---

## #6. P3 방문 리캡

```
[#0 문구]

P3 방문 리캡 화면을 만들어. 라우트 /recap. GET /recap 사용.

구성
1) 헤더 — 방문일과 매장명, mono 라벨
2) 오늘 살펴본 제품 — 스캔 순서대로. 각 항목에 스캔 횟수를 mono로 병기. 순서가 정보이므로 01/02/03 넘버링 사용(여기서는 실제 순서라 정당함)
3) 관심 경향 — 서버의 interestSummary 문장. aiUsed=false면 "관측 기록 기준"이라는 mono 캡션을 붙여 투명하게 표시
4) 미해결 요인 — unresolvedFactors를 --seal 컬러 좌측 바로. 화면 전체에서 seal은 이 섹션에만
5) 기록 연결 — 계정 연결 유도. 문구의 논리는 "혜택"이 아니라 "기록을 잃지 않기 위해". 이 시점까지 개인정보를 요구하지 않았다는 사실을 명시
6) 하단 액션 — "저장 목록 이어보기"(→ /archive)

계정 연결 UI
- 이메일 또는 국가번호 포함 전화 중 선택, 동의 체크박스 1개
- POST /recap/link. 실패해도 리캡 내용은 계속 보인다
- 건너뛰기가 항상 가능해야 한다. 모달로 막지 마

백엔드도 함께: GET /recap, POST /recap/link 구현. 연결 값은 SHA-256 해시만 저장하고 원문 컬럼을 만들지 마.
```

---

## #7. P4 국경 이전 — 가장 중요

```
[#0 문구]

P4 국경 이전 화면을 만들어. 라우트 /transfer. 이 화면이 데모의 클라이맥스다. 다른 화면보다 완성도를 높게 잡아.

구성
1) 헤더 2행 — "SEOUL / SEONGSU" → 대상 시장. 두 줄 사이에 화살표가 아니라 얇은 수평선 + mono 국가코드
2) 이전 목록 — 저장 제품별 카드(카드 아님, hairline 구분 섹션)
   - IN_STOCK: 현지 매장명 + "매장 방문 예약"
   - TRANSFERABLE: "서울에서 N일 내 이동 가능" + "이동 요청"
   - ONLINE_ONLY: "현지 온라인 스토어에서 주문 가능" + "온라인 스토어에서 주문"
   - 상태별 컬러는 DESIGN_SYSTEM 6장 StockStatus 규격
3) 미해결 요인 답변 — 서울에서 답을 얻지 못한 질문 1~2개와 그 답변. 질문을 먼저, 답을 아래에
4) 발송 시점 근거 — sendTiming.rationale을 mono 캡션으로 작게

핵심 인터랙션 (여기에 시간을 써)
- 화면 진입 시 저장 카드들이 좌→우로 12px 이동하며 시장 코드·통화·재고 상태가 KR → 대상 시장으로 교체되는 전환. 총 1.2초, 카드당 120ms 스태거
- 같은 타이밍에 하단 MrzBar가 MKT<KR → MKT<HK 로 스크램블 전환
- 전환 후 상태가 안정되면 액션 버튼이 페이드인
- prefers-reduced-motion에서는 최종 상태만 즉시 표시

백엔드도 함께: GET /transfer?market= 구현. InventoryPort로 4개 시장 조회. sendTiming은 지금은 규칙 기반(마지막 스캔 + 72시간).

데모 편의: /transfer?market=HK 처럼 쿼리로 시장을 강제할 수 있게 하고, ?replay=1 이면 전환 애니메이션을 처음부터 다시 재생.
```

---

## #8. AI 연동

```
[#0 문구]

docs/AI_SPEC.md 4장의 AI-1~AI-5를 실제 호출로 구현해. CLAUDE.md R5, R6을 지켜라.

1) ai/AnthropicClient
- WebClient로 https://api.anthropic.com/v1/messages 호출
- 헤더: x-api-key(${ANTHROPIC_API_KEY}), anthropic-version: 2023-06-01
- 모델은 application.yml의 entry.ai.model 프로퍼티로 주입 (기본값은 최신 문서 확인 후 설정하고, 하드코딩하지 말 것)
- 타임아웃 8초, 재시도 1회, 실패 시 AiUnavailableException
- entry.ai.mock=true 면 MockAiClient가 고정 응답 반환 (개발 중 기본값 true)

2) resources/prompts/*.md 로 프롬프트 분리
- intent-signal.md (AI-1): 스캔 시퀀스 → stage/comparisonAxis/unresolved/rationale JSON. 스키마를 프롬프트에 명시하고 JSON만 출력하도록 지시
- greeting.md (AI-2): 캐릭터별 톤 정의, 1문장, 구매 권유 금지, 결정 기준 질문으로 끝낼 것
- conversation-system.md (AI-2): 최대 3턴, 판매하지 않음, 결정 기준 정리에 한정, 사실이 불확실하면 직원 호출 안내
- recap-summary.md: 스캔 목록 → 관심 경향 1~2문장 + 미해결 요인
- transfer-message.md (AI-4): 저장 목록 + 시장별 재고 → 이전 메시지와 미해결 답변
- advisor-briefing.md (AI-5): 응대용 1~2문장 요약 + 핵심 표현 병기

3) 폴백 규칙
- 모든 AI 지점에 규칙 기반 대체 구현이 있어야 한다
- 폴백 사용 시 응답 meta.aiUsed=false, meta.fallback=true
- 프론트는 aiUsed=false일 때 문구를 "관측 기록 기준"으로 캡션 표시. 에러 화면을 띄우지 마

4) 언어 자동 전환 (AI-3)
- 브랜드 승인 문구 집합(i18n JSON)을 우선 사용하고, 없는 키만 AI가 조정
- 소재명·라인명은 원어 유지. 프롬프트에 "다음 용어는 번역하지 마" 목록 포함
- 번역 결과를 그대로 노출하지 않도록 승인 문구 우선순위를 코드로 강제

5) 대화 3턴 제한을 서버에서 강제. 초과 시 handoffSuggested=true 반환하고 생성하지 않는다

완료 조건: ANTHROPIC_API_KEY 없이 전 화면이 폴백으로 정상 동작. 키를 넣으면 문장이 달라진다. 두 경우 모두 스크린 크래시 없음.
```

---

## #9. P6 대화 + P8 사전 등록 + P5 아카이브

```
[#0 문구]

세 화면을 순서대로 만들어. 하나 끝날 때마다 커밋하고 다음으로.

P6 /talk/:scanId
- 캐릭터 말풍선과 사용자 입력이 교차하는 3턴 대화. 남은 턴 수를 mono로 표시
- 3턴 소진 또는 handoffSuggested면 "직원 호출" 액션으로 전환. 더 입력받지 않는다
- 대화에서 추출된 결정 기준을 화면 하단에 의도 카드로 요약 표시
- 대화를 시작하지 않아도 P1의 정보는 그대로 남아있어야 한다. 대화는 관문이 아니라 선택지

P8 /register
- 광고·소셜 진입 화면. 상단에 팝업 정보, 관심 라인 다중 선택, 연락 수단, 동의 체크박스
- 제공 혜택은 "전용 시간대 우선 입장"만. 할인·쿠폰 표현 금지
- 등록 완료 시 우선 입장 코드와 시간대를 여권 데이터면과 같은 서식으로 표시

P5 /archive
- 저장 제품 목록. 각 항목에 저장 시점의 매장과 구역을 mono 캡션으로
- 상단에 관심 경향 요약 1문장
- 항목별 본국 시장 상태 배지, "국경 이전 보기"(→ /transfer) 진입점
- 빈 상태: "아직 저장한 제품이 없습니다. 매대 태그를 스캔하세요."
```

---

## #10. D1 대시보드 + P7 어드바이저 + D2 콘솔

```
[#0 문구]

운영자 화면을 만들어. DESIGN_SYSTEM 9장에 따라 여권 은유를 쓰지 말고 밀도 높은 데이터 레이아웃으로.

D1 /admin (데스크톱 1440 기준, 12열 그리드)
- 상단 KPI 4개: 총 스캔, 재스캔률, 저장률, 국경 이전 회수율. 숫자는 mono
- 제품별 의도 강도 테이블 (스캔·재스캔률·저장률·전환율, 정렬 가능)
- 미해결 요인 분포 막대 차트 (Recharts, 색 2개만)
- 시장별 방문 분포
- 구역별 체류·저장
- 하단에 actionHints를 운영 지시 문장으로 표시 — 대시보드가 관찰로 끝나지 않고 매장 운영 변경으로 이어진다는 점을 화면으로 보여주는 영역이다

P7 /advisor/:grantToken (태블릿 가로 기준)
- 브리핑 1~2문장을 가장 크게
- 미해결 요인, 저장 목록, 고객 언어와 핵심 표현 병기
- 상담 메모 입력 → POST /advisor/{token}/notes
- 만료 남은 시간 카운트다운. 만료되면 화면을 잠그고 재발급 안내

D2 /admin/personas
- 페르소나 5종 카드, 가설 선택(H2/H3/H5), 변형 A/B 선택 후 실행
- 결과를 표로 (변형별 저장 여부, 이유, 미해결 요인)
- 서버가 내려주는 disclaimer를 화면 상단에 항상 노출. 숨기거나 접지 마

백엔드: GET /admin/intent-dashboard, POST /consents/advisor, GET/POST /advisor/{token}, GET /personas, POST /personas/{id}/simulate 구현.
페르소나 시뮬레이션은 AnthropicClient로 페르소나 설정 + 화면 요약을 넣고 저장 여부·이유를 JSON으로 받는다. mock 모드에서도 그럴듯한 고정 결과를 반환해.
```

---

## #11. QR 시트 + 데모 모드

```
[#0 문구]

발표 시연용 자산을 만들어.

1) /dev/qr
- qrcode 패키지로 클라이언트에서 QR 생성
- 제품 20종은 {ORIGIN}/s/{productId}, 구역 4개는 {ORIGIN}/z/{zoneId}
- A4 인쇄 레이아웃(@media print): QR 40mm, 아래 제품명과 코드 mono로. 페이지 나눔 처리
- ORIGIN은 window.location.origin 사용 (배포 URL로 자동 반영)

2) /dev/reset
- 세션 초기화(localStorage 삭제 + 새 세션 발급)로 리허설을 반복 가능하게
- 시나리오 프리셋 버튼: "홍콩 고객 3스캔 상태", "전 구역 방문 상태", "빈 상태"

3) 데모 폴백 모드
- ?demo=1 이면 API 호출 없이 web/src/features/*/demoData.ts 의 고정 데이터로 전 화면이 동작
- 네트워크 실패 시 자동으로 demo 모드로 강등되고, 화면 상단에 mono 캡션 한 줄로만 알린다. 에러 모달 금지

4) README에 시연 순서를 5줄로 정리
```

---

## #12. 배포 준비

```
[#0 문구]

배포 설정을 추가해. BUILD_STEPS.md Phase 8을 따른다.

- api/Dockerfile (멀티스테이지, gradle:8-jdk21 → temurin:21-jre)
- api/src/main/resources/application-prod.yml: Postgres, server.port=${PORT:8080}, ddl-auto=update, entry.cors.origins=${ENTRY_CORS_ORIGINS}
- web/vercel.json: SPA rewrite
- web/.env.example, api/.env.example (실제 키는 넣지 마)
- .github/workflows/ci.yml: web은 npm ci + build, api는 gradlew build. push/PR에서 실행
- README에 배포 절차와 필요한 환경변수 표

확인: 로컬에서 docker build 성공, npm run build 성공, gradlew build 성공.
ANTHROPIC_API_KEY가 커밋에 포함되지 않았는지 git log -p로 점검해.
```

---

## 프롬프트 사용 팁

| 상황 | 대응 |
|---|---|
| 에이전트가 룰북을 무시하고 그림자·둥근 모서리를 넣음 | `docs/DESIGN_SYSTEM.md 4장을 다시 읽고, 위반한 부분을 찾아서 수정해` |
| 파일이 너무 커져서 수정이 불안정 | `이 화면을 컴포넌트 3개로 분리하고 각 파일 150줄 이하로 유지해` |
| 컨텍스트가 길어져 품질 저하 | `/clear` 후 #0 문구 + 다음 프롬프트만 |
| API 스펙이 코드와 어긋남 | `docs/API_CONTRACT.md와 실제 컨트롤러를 비교해서 불일치 목록을 먼저 보고해. 수정은 승인 후에` |
| 없는 기능을 만들어옴 | `PRD에 없는 기능이다. 되돌리고 PRD 범위만 유지해` |
| 디자인이 평범하게 나옴 | `DESIGN_SYSTEM 5장 시그니처 요소가 화면에서 안 보인다. MRZ 밴드와 스탬프를 규격대로 살려` |
