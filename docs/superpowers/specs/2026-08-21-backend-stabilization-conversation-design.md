# ENTRY 백엔드 안정화 및 P6 대화 기능 설계

작성일: 2026-08-21
대상 브랜치: `codex/backend-stabilization-conversation`

## 1. 목표

현재 구현된 P1 스캔·아카이브·패스포트 흐름에서 데이터 유실과 잘못된 요청 처리를 바로잡고, P1 화면의 `대화 이어가기` 링크가 실제 P6 구매 고민 대화로 연결되게 한다.

이번 작업의 완료 조건은 다음과 같다.

- 무효 세션이 자동 교체되어도 현재 요청의 스캔·저장 기록을 계속 조회할 수 있다.
- 존재하지 않는 패스포트 존, 잘못된 UUID·enum·JSON은 서버 오류가 아니라 명확한 4xx로 응답한다.
- 패스포트 번호와 세션당 패스포트가 DB 제약으로 중복되지 않는다.
- 스캔에서 시작한 대화를 최대 3턴 진행하고, 결정 기준과 미해결 요인을 응답한다.
- 실제 Anthropic 호출, 개발용 모의 응답, 장애 시 규칙 기반 폴백이 같은 인터페이스를 사용한다.
- 프론트 `/talk/:scanId`가 백엔드 대화 API와 연결된다.
- API 계약, 백엔드 통합 테스트, 프론트 빌드가 함께 통과한다.

## 2. 범위

### 포함

- 세션 자동 교체 프로토콜 수정
- 공통 입력 오류 처리 확장
- 아카이브 `scanId` 검증
- 패스포트 존 검증과 유니크 제약
- P6 대화 엔티티·서비스·컨트롤러·DTO
- Anthropic Messages API 클라이언트와 모의·폴백 구현
- P6 최소 모바일 화면과 API 클라이언트
- `docs/API_CONTRACT.md` 동기화
- 핵심 흐름 자동 테스트

### 제외

- P7 어드바이저, P8 사전 등록, D1 대시보드, D2 페르소나봇
- 로그인, 결제, 실재고·예약 시스템 연동
- 관리자 인증과 운영용 관측 플랫폼
- 기존 P2~P5 전체 화면 구현

## 3. 구성 원칙

- 기존 도메인 기준 패키지 구조를 유지한다.
- 새 라이브러리는 추가하지 않는다. 이미 포함된 Spring WebClient, Jackson, JPA, Validation을 사용한다.
- 컨트롤러는 요청 검증과 응답 조립만 맡고 세션 소유권·턴 제한·AI 폴백은 서비스에서 처리한다.
- 프론트 컴포넌트는 `fetch`를 직접 호출하지 않고 `features/conversation/api.ts`를 사용한다.
- 텍스트는 i18n과 브랜드 콘텐츠에서 가져오며, API 키는 백엔드 환경변수로만 전달한다.
- AI 응답에 제품 소재·무게 등 사실이 필요하면 `ProductCatalog`의 데이터만 제공한다.

## 4. 세션 자동 교체

### 현재 문제

무효한 `X-Entry-Session`이 들어오면 서버는 새 세션 B로 현재 요청을 처리한다. 그러나 프론트는 `meta.sessionRotated=true`를 본 뒤 별도의 세션 C를 발급한다. 현재 요청의 기록은 B에 남고 사용자는 C를 저장하므로 기록이 유실된다.

### 변경안

1. `SessionInterceptor`가 세션 B를 자동 발급한 즉시 응답 헤더 `X-Entry-Session`에 B의 ID를 설정한다.
2. 정상 세션 요청에도 같은 응답 헤더를 내려 클라이언트 동작을 단순화한다.
3. CORS 설정에서 `X-Entry-Session`을 노출한다.
4. 프론트 공통 클라이언트는 응답 헤더의 세션 ID를 `replaceSession`으로 즉시 저장하되, 부팅 완료(`ready`)는 현재 세션 조회가 끝난 후에만 활성화한다.
5. `meta.sessionRotated`는 화면 분석과 디버깅을 위해 유지하지만 추가 세션 발급은 하지 않는다.
6. 앱 부팅 시 `GET /sessions/current`로 저장된 세션을 먼저 확정하고, 중복 부팅 호출은 하나의 Promise로 공유해 평행 자동 발급을 막는다.

이 방식은 현재 요청과 이후 요청이 항상 같은 세션을 사용하게 하며, 별도 재요청 경쟁 상태도 제거한다.

## 5. 입력 오류와 패스포트 안정화

### 공통 오류

다음 예외를 `GlobalExceptionHandler`에서 400으로 변환한다.

- JSON 형식 또는 enum 역직렬화 실패: `INVALID_REQUEST_BODY`
- 경로·쿼리 enum 변환 실패: `INVALID_PARAMETER`
- 잘못된 UUID 형식: 도메인별 코드 또는 `INVALID_PARAMETER`

예상하지 못한 예외만 500으로 유지한다. 서버 로그에는 requestId와 예외를 남기되 응답에는 내부 정보를 노출하지 않는다.

### 아카이브

`scanId`가 비어 있지 않으면 UUID 형식을 먼저 검증한다. 형식이 틀리면 `INVALID_SCAN_ID` 400을 반환한다. 다른 세션의 스캔 또는 존재하지 않는 스캔은 제품 저장 자체를 막지 않고 기본 매장·존으로 저장하는 기존 정책을 유지한다.

### 패스포트

- `ZoneCatalog.require(zoneId)`를 추가하고 알 수 없는 존은 `INVALID_ZONE` 400으로 거절한다.
- 검증된 정규화 존 ID만 중복 검사와 등급 계산에 사용한다.
- `passport.sessionId`, `passport.passportNo`, `passport_stamp(passportId, zoneId)`에 DB 유니크 제약을 둔다.
- 패스포트 번호는 전체 행 개수 대신 세션 UUID 전체를 하이픈 없이 붙인 `ENT-KR-{32자}` 형식을 사용한다. 같은 세션의 동시 발급은 세션 행 잠금으로 직렬화하고, 서로 다른 세션은 UUID 전체를 써서 번호 충돌을 막는다.

## 6. P6 구매 고민 대화

### API

#### `POST /api/v1/conversations`

요청:

```json
{ "scanId": "uuid" }
```

처리:

- `scanId`가 현재 세션의 스캔인지 검증한다.
- 같은 세션·스캔에 열린 대화가 있으면 기존 대화를 반환해 중복 생성을 막는다.
- 스캔 당시 제품과 캐릭터를 기준으로 첫 안내 메시지를 만든다.

응답 `data`:

```json
{
  "conversationId": "uuid",
  "turnsRemaining": 3,
  "messages": [
    { "role": "CHARACTER", "character": "KAISER", "text": "어떤 점이 가장 고민되시나요?" }
  ]
}
```

#### `POST /api/v1/conversations/{conversationId}/messages`

요청:

```json
{ "text": "화이트 컬러 관리가 어려울 것 같아요." }
```

응답 `data`:

```json
{
  "reply": { "character": "KAISER", "message": "..." },
  "turnsRemaining": 2,
  "extracted": { "criteria": ["관리 용이성"], "unresolved": "COLOR_CARE" },
  "handoffSuggested": false
}
```

### 저장 모델

`Conversation`

- `id`, `sessionId`, `scanId`, `productId`, `character`, `turnCount`, `createdAt`, `updatedAt`
- `(sessionId, scanId)` 유니크

`ConversationMessage`

- `id`, `conversationId`, `role`, `text`, `createdAt`
- 대화 조회 시 생성 시각 순으로 반환

대화 엔티티는 원문 연락처나 사용자 프로필을 저장하지 않는다. 구매 고민 텍스트만 저장하며 한 메시지는 500자로 제한한다.

### 턴 제한과 소유권

- 사용자 메시지를 정상 처리할 때마다 `turnCount`를 1 증가시킨다.
- 3턴이 끝나면 모델을 더 호출하지 않고 직원 상담 안내를 반환한다.
- 다른 세션이 대화 ID를 조회하거나 메시지를 보내면 존재 여부를 노출하지 않도록 `CONVERSATION_NOT_FOUND` 404를 반환한다.

## 7. AI 호출 구조

`ConversationAiClient` 인터페이스가 `analyze(context)`를 제공한다.

- `MockConversationAiClient`: `entry.ai.mock=true`일 때 고정된 구조화 응답을 반환한다.
- `AnthropicConversationAiClient`: `entry.ai.mock=false`일 때 WebClient로 Anthropic Messages API를 호출한다.
- `RuleConversationFallback`: 네트워크 오류, 8초 시간 초과, 응답 파싱 실패 시 제품 정보와 키워드 규칙으로 답한다.

프롬프트는 `resources/prompts/conversation.md`에서 읽는다. 모델에는 다음 정보만 전달한다.

- 제품명, 소재, 무게, 크기, craft note
- 현재 세션의 해당 대화 메시지
- 캐릭터와 언어

응답 JSON은 `message`, `criteria`, `unresolved` 세 필드를 요구한다. 파싱 실패 시 즉시 규칙 기반 답변으로 전환해 사용자 대기 시간을 늘리지 않는다. 폴백 여부는 공통 `meta.aiUsed`, `meta.fallback`에 반영한다.

시스템 지침에는 구매 강요·할인 제안 금지, 결정 기준 정리에 한정, 제공되지 않은 제품 사실 생성 금지를 포함한다.

## 8. 프론트 연결

- `features/conversation/api.ts`에 대화 시작과 메시지 전송 함수를 추가한다.
- `/talk/:scanId` 라우트와 `Talk.tsx` 화면을 추가한다.
- 화면 진입 시 대화를 시작하고 기존 첫 메시지를 표시한다.
- 입력 전송 중 중복 제출을 막고, 실패하면 재시도 가능한 오류 문구를 표시한다.
- `turnsRemaining=0` 또는 `handoffSuggested=true`이면 입력을 닫고 직원 상담 안내를 표시한다.
- 한국어·영어 문구를 i18n 파일에 추가한다.

P6 화면은 기존 `MobileFrame`, `CharacterBubble`, `Button`, `Field` 계열을 재사용하고 새로운 UI 라이브러리를 도입하지 않는다.

## 9. 테스트

### 백엔드 통합 테스트

- 무효 세션 요청의 응답 헤더와 실제 저장 세션이 일치한다.
- 잘못된 시장 enum·JSON·scanId가 각각 400을 반환한다.
- 존재하지 않는 존은 적립되지 않고 400을 반환한다.
- 같은 존 중복 적립과 60초 제한이 기존 상태 코드로 동작한다.
- 세션당 패스포트가 하나만 발급된다.
- 다른 세션의 scanId로 대화를 시작할 수 없다.
- 같은 scanId 대화 시작이 멱등적이다.
- 3턴 이후 AI가 호출되지 않고 handoff가 활성화된다.
- AI 오류 시 규칙 기반 응답과 `fallback=true`가 반환된다.

### 프론트 검증

- TypeScript 빌드
- ESLint
- 응답 헤더 세션 교체 로직 단위 검증이 가능하도록 순수 함수로 분리
- 대화 API 타입이 `API_CONTRACT.md`와 일치하는지 수동 점검

## 10. 배포 설정

- `ANTHROPIC_API_KEY`: 운영에서만 주입
- `ENTRY_AI_MOCK`: 로컬 기본값 `true`, 시연 환경에서 실제 AI를 쓰면 `false`
- `ENTRY_AI_MODEL`: 환경변수로 모델 ID 지정
- `ENTRY_AI_TIMEOUT_SECONDS`: 기본 8초

API 키가 없는데 `ENTRY_AI_MOCK=false`이면 애플리케이션 기동을 실패시키지 않고 규칙 기반 폴백만 사용한다. AI 장애가 핵심 사용자 흐름을 막지 않아야 한다.

## 11. 완료 기준

- `api`의 `./gradlew clean test` 성공
- `web`의 `npm run build`, `npm run lint` 성공
- 세션 교체 후 스캔 → 아카이브 → P6 대화 흐름을 통합 테스트로 검증
- Swagger와 `docs/API_CONTRACT.md`의 엔드포인트·필드 일치
- 비밀값, 실제 PII, 할인·쿠폰 필드가 코드와 응답에 없음
