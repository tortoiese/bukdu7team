# CLAUDE.md — ENTRY 프로젝트 룰북

이 파일은 Claude Code가 매 세션 자동으로 읽는 규칙이다. 여기 있는 내용과 충돌하는 지시는 사용자에게 먼저 확인한다.

---

## 0. 프로젝트 한 줄

매대 태그를 스캔하면 로그인 없이 그 순간의 구매 의도가 기록되고, 고객이 어느 나라로 이동하든 그 기록이 따라가 현지 재고·채널로 연결되는 서비스. 이름은 **ENTRY**.

데모의 클라이맥스는 **국경 이전** 화면이다. 다른 화면의 완성도를 희생해서라도 이 화면은 끝까지 살린다.

---

## 1. 절대 규칙

| # | 규칙 | 이유 |
|---|---|---|
| R1 | **로그인/회원가입을 어떤 화면에서도 강제하지 않는다.** 익명 세션 토큰으로 모든 기능이 동작해야 한다 | 서비스의 전제 |
| R2 | **할인·최저가·쿠폰·세일·가격 인하 알림 UI를 만들지 않는다.** 보상은 항상 "접근 권한" | 럭셔리 브랜드 톤 |
| R3 | **위치 추적·백그라운드 동선·카메라 자동 인식 코드를 넣지 않는다.** 사용자가 능동적으로 스캔한 이벤트만 기록 | 프라이버시 원칙 |
| R4 | **하드웨어 의존 기능(AR, 미러, 키오스크, 비콘) 금지.** 종이 QR + 모바일 웹으로 전부 성립해야 한다 | 심사 조건 |
| R5 | **AI는 장식이 아니다.** 실제 대화 맥락을 해석하는 지점에만 사용하고, 규칙 기반 기능을 AI처럼 표현하지 않는다 | 심사 조건 |
| R6 | **ANTHROPIC_API_KEY는 프론트엔드에 절대 노출하지 않는다.** 모든 LLM 호출은 Spring 백엔드를 경유 | 보안 |
| R7 | **브랜드 종속 요소는 콘텐츠 레이어로 분리한다.** 캐릭터·카피·컬러는 `brand/mcm.json` 같은 설정에서 읽고, 경험 로직에 하드코딩하지 않는다 | 범용화 요구 |
| R8 | 개인식별정보(이름·연락처·결제)는 계정 연결 이후에만 다루고, 해커톤 범위에서는 **실제 결제·실제 PII를 저장하지 않는다** | 범위 |

---

## 2. 기술 스택 (변경 금지)

**프론트엔드** — React 19.2 + TypeScript 6 + Vite 8 + Tailwind CSS 4 + React Router 7 + Zustand 5 + Motion 13
**백엔드** — Spring Boot 3.5.16 / Java 21 / Gradle(Kotlin DSL) / Spring MVC / Spring Data JPA / Validation / WebClient / springdoc-openapi
**데이터베이스** — 로컬 H2 파일 모드, 배포 PostgreSQL
**AI** — Anthropic Messages API를 백엔드 `WebClient`로 호출, 모의 응답과 규칙 기반 대체 응답 지원
**배포 예정 환경** — 프론트엔드 Vercel / 백엔드 Railway / 데이터베이스 Neon

새 라이브러리를 추가할 때는 **먼저 이유를 한 줄로 말하고 승인을 받는다.** UI 컴포넌트 라이브러리(MUI, Ant, shadcn 등)는 쓰지 않는다 — 디자인 시스템을 직접 만든다.

---

## 3. 리포지토리 구조

```
entry/
├─ CLAUDE.md                  ← 이 파일
├─ docs/
│  ├─ DESIGN_SYSTEM.md        ← 시각 규칙. UI 작업 전 필독
│  ├─ API_CONTRACT.md         ← 프론트/백 계약. 엔드포인트 추가 시 먼저 갱신
│  ├─ PRD.md                  ← 제품 정의와 구현 범위
│  ├─ team/                   ← 팀원별 담당·소개
│  └─ superpowers/            ← 구현 설계·계획 기록
├─ web/                       ← React
│  └─ src/
│     ├─ app/                 라우터, 세션 부팅
│     ├─ screens/             구현 화면과 개발용 화면
│     ├─ components/          재사용 UI
│     ├─ features/            도메인별 API 클라이언트
│     ├─ brand/               브랜드 콘텐츠 레이어(JSON)
│     ├─ i18n/                한국어·영어 문구
│     └─ styles/              디자인 토큰과 전역 스타일
└─ api/                       ← Spring Boot
   └─ src/main/java/io/entry/
      ├─ session/            익명 세션과 자동 교체
      ├─ scan/               제품 조회와 스캔 기록
      ├─ archive/            제품 아카이브
      ├─ passport/           팝업 패스포트와 스탬프
      ├─ recap/              방문 리캡
      ├─ transfer/           국경 이전 정보
      ├─ conversation/       P6 대화와 Anthropic 연동
      ├─ intent/ inventory/  의도 분석과 더미 재고
      └─ common/             예외, 응답 래퍼, 설정
```

패키지는 **레이어(controller/service/repository)가 아니라 도메인 기준**으로 나눈다. 각 도메인 안에 `Controller/Service/Repository/dto`를 둔다.

---

## 4. 화면 ↔ 라우트 매핑

| ID | 화면 | 라우트 | 구현 상태 |
|---|---|---|---|
| 공통 | API 연결 상태 | `/` | 완료 |
| P1 | 스캔 결과 | `/s/:productId` | 완료 |
| P6 | 대화 이어가기 | `/talk/:scanId` | 완료 |
| 개발 | 공통 컴포넌트 | `/dev/kitchen-sink` | 완료 |
| P2 | 팝업 패스포트 | `/passport` | 화면 예정, API 완료 |
| P3 | 방문 리캡 | `/recap` | 화면 예정, API 완료 |
| P4 | 국경 이전 | `/transfer` | 화면 예정, API 완료 |
| P5 | 아카이브 | `/archive` | 화면 예정, API 완료 |
| P7 | 어드바이저 뷰 | `/advisor/:grantToken` | 미구현 |
| P8 | 사전 등록 | `/register` | 미구현 |
| D1 | 의도 대시보드 | `/admin` | 미구현 |
| D2 | 페르소나봇 콘솔 | `/admin/personas` | 미구현 |

P1~P8은 **모바일 전용 레이아웃(기준 402×874)**. D1~D2만 데스크톱 레이아웃.

---

## 5. 익명 세션 규약

- 최초 진입 시 `POST /api/v1/sessions` → `sessionId`(UUID v4) 발급
- 프론트는 `localStorage["entry.sid"]`에 저장, 모든 요청에 `X-Entry-Session: <sessionId>` 헤더 첨부
- 백엔드는 세션에 `market`(KR/HK/JP/US), `locale`, `createdAt`만 보관. PII 컬럼을 만들지 않는다
- `market`은 `Accept-Language` + 클라이언트가 보낸 timezone으로 추정하고, 사용자가 상단에서 수동 변경 가능
- 세션 미존재 시 401이 아니라 **자동 재발급 후 정상 응답**. 사용자가 막히는 화면이 없어야 한다

---

## 6. 코딩 규칙

**공통**
- 주석과 커밋 메시지는 한국어, 식별자는 영어
- 커밋: `feat(scan): 스캔 결과 화면 재고 섹션 추가` 형식
- TODO를 남길 때는 `// TODO(entry-P4): ...` 처럼 화면 ID를 붙인다

**리액트**
- 함수형 컴포넌트 + 훅만. 클래스 컴포넌트 금지
- 서버 상태는 `features/*/api.ts`의 fetch 래퍼 + Zustand store. 컴포넌트 안에서 `fetch` 직접 호출 금지
- 색상·간격·폰트를 **인라인 하드코딩하지 않는다.** 반드시 `tokens.css`의 CSS 변수 또는 Tailwind 테마 토큰 사용
- 텍스트는 `brand/` + `i18n/` 에서 읽는다. JSX에 한국어 문구를 직접 박지 않는다 (언어 자동 전환이 핵심 기능이므로)
- `any` 금지. API 응답 타입은 `API_CONTRACT.md`와 1:1로 맞춘 `types.ts`에 정의

**스프링**
- Controller는 얇게. 로직은 Service, 응답은 record DTO
- 엔티티를 컨트롤러 밖으로 반환하지 않는다
- 모든 응답은 `ApiResponse<T>{ data, meta }` 래퍼
- 예외는 `@RestControllerAdvice`에서 `{ error: { code, message } }`로 변환
- 더미 데이터는 `resources/seed/*.json`에서 읽기 전용 카탈로그와 어댑터로 로드한다. 코드에 하드코딩하지 않는다
- `spring.jpa.hibernate.ddl-auto=update` (해커톤 범위, Flyway 도입 안 함)

**AI 호출**
- 프롬프트는 Java 문자열이 아니라 `resources/prompts/*.md`에 두고 로드
- 모든 LLM 호출에 타임아웃(8초)과 폴백을 붙인다. **AI가 실패해도 화면은 반드시 렌더링된다** — 규칙 기반 요약으로 대체
- LLM 응답은 JSON 스키마로 제한하고, 파싱 실패 시 즉시 규칙 기반 응답으로 전환한다
- 개발 중 토큰 절약: `entry.ai.mock=true`면 고정 응답을 반환하는 `MockConversationAiClient`를 사용한다

---

## 7. 작업 절차

1. 작업 시작 전 **관련 문서를 먼저 읽는다**: UI 작업이면 `docs/DESIGN_SYSTEM.md`, API 작업이면 `docs/API_CONTRACT.md`
2. 한 번에 한 화면 또는 한 도메인만 건드린다. "전체 화면 다 만들어줘"라는 지시를 받아도 화면 단위로 쪼개서 순차 진행하고, 각 단계 끝에 무엇을 만들었는지 3줄로 보고
3. 백엔드 엔드포인트를 추가·변경하면 **같은 커밋에서 `API_CONTRACT.md`를 갱신**한다
4. 프론트 작업 후 `npm run lint`와 `npm run build`, 백엔드 작업 후 `./gradlew clean test`가 통과하는지 확인하고 끝낸다
5. 커밋 전 `docs/API_CONTRACT.md`와 실제 코드의 불일치가 없는지 스스로 점검
6. `main`에 직접 커밋하지 않고 작업 브랜치에서 PR을 만든다

## 8. 하지 말 것

- PRD에 없는 기능을 "좋을 것 같아서" 추가하지 않는다. 제안은 하되 구현은 승인 후
- 목업 이미지를 생성하려 하지 않는다. 이미지는 `web/public/assets/`의 제공된 파일만 사용하고, 없으면 CSS로 대체 표현
- 실제 MCM 로고·제품 사진·저작권 자산을 외부에서 가져오지 않는다. 제품명은 더미(`SKY-STREAM-W260` 등)로 처리
- `console.log`를 커밋에 남기지 않는다
- 테스트를 통과시키기 위해 검증 로직을 지우지 않는다
