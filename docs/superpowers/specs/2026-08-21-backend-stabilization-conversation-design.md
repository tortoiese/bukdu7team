# ENTRY 백엔드 안정화와 P6 대화 기능 설계 기록

## 문서 상태

- 작성일: 2026년 8월 21일
- 구현 상태: 완료
- 구현 커밋: `85f7f24`
- 관련 계획: `docs/superpowers/plans/2026-08-21-backend-stabilization-conversation.md`

이 문서는 구현 전에 결정한 설계와 실제 반영 결과를 함께 기록한다. 현재 API의 정확한 요청·응답은 `docs/API_CONTRACT.md`를 기준으로 한다.

## 1. 목표

- 무효 세션 자동 교체 과정의 데이터 유실과 중복 세션 문제를 해결한다.
- 잘못된 입력이 500 오류로 번지는 경로를 400 오류로 정리한다.
- 패스포트 발급과 존 스탬프의 중복·동시성 문제를 막는다.
- P1에서 시작한 구매 고민을 P6의 3턴 대화로 이어 준다.
- AI 장애가 발생해도 사용자 흐름이 중단되지 않게 한다.

## 2. 최종 구조

### 세션

- `SessionInterceptor`가 요청에 사용할 세션을 먼저 확정한다.
- 세션이 없거나 무효하면 새 세션을 발급한다.
- 확정한 ID를 요청 컨텍스트와 `X-Entry-Session` 응답 헤더에 동일하게 기록한다.
- 프론트엔드는 응답 헤더를 `localStorage["entry.sid"]`에 즉시 반영한다.
- `GET /api/v1/sessions/current`가 화면 요청 전에 세션을 확정해 병렬 요청의 회전 경쟁을 방지한다.

### 입력 검증

- 잘못된 JSON과 enum은 각각 `INVALID_REQUEST_BODY`, `INVALID_PARAMETER` 400으로 변환한다.
- 잘못된 `scanId`는 `INVALID_SCAN_ID` 400으로 변환한다.
- 예상하지 못한 예외만 `INTERNAL_ERROR` 500으로 남긴다.

### 패스포트

- 서버 존 카탈로그에 없는 값은 `INVALID_ZONE` 400으로 거절한다.
- 세션별 패스포트와 패스포트 번호에 유일성 제약을 둔다.
- 패스포트 번호는 세션 UUID 전체를 사용해 충돌 가능성을 없앤다.
- 같은 세션의 발급과 같은 패스포트의 스탬프 처리는 DB 잠금으로 직렬화한다.

### P6 대화

- `POST /api/v1/conversations`로 스캔에 연결된 대화를 시작하거나 복원한다.
- `POST /api/v1/conversations/{conversationId}/messages`로 메시지를 전송한다.
- 대화는 세션 소유권을 검증하고 사용자 메시지를 최대 3턴까지 처리한다.
- 같은 대화의 동시 메시지는 잠금 조회로 직렬화해 3턴을 초과해 저장되지 않게 한다.
- 3턴이 끝나면 AI를 더 호출하지 않고 직원 상담 연결을 안내한다.
- 메시지는 500자로 제한하고 실제 연락처나 사용자 프로필을 저장하지 않는다.

## 3. AI 처리

`ConversationAiClient`를 기준으로 구현을 분리한다.

- `MockConversationAiClient`: 로컬과 테스트에서 결정적인 모의 응답 제공
- `AnthropicConversationAiClient`: 백엔드에서 Anthropic Messages API 호출
- `RuleConversationFallback`: 키 누락, 시간 초과, 네트워크 오류, 응답 파싱 오류 시 규칙 기반 답변 제공
- `ConversationAiService`: AI 결과 검증과 대체 응답 전환 담당

프롬프트는 `api/src/main/resources/prompts/conversation.md`에서 관리한다. 모델에는 제품 정보, 현재 대화 기록, 캐릭터, 언어만 전달한다. 할인 제안, 구매 강요, 제공되지 않은 제품 정보 생성은 금지한다.

## 4. 프론트엔드 연결

- `/talk/:scanId`에 모바일 대화 화면을 구현했다.
- `features/conversation/api.ts`를 통해서만 대화 API를 호출한다.
- 전송 중 중복 제출을 막고 실패 시 재시도 안내를 표시한다.
- 남은 턴이 0이면 입력을 닫고 직원 상담 안내를 표시한다.
- 한국어와 영어 문구를 i18n 파일에서 관리한다.

## 5. 검증 결과

- 세션 자동 교체 후 응답 헤더와 저장 데이터의 세션 일치
- 현재 세션 선확정으로 병렬 요청의 중복 세션 방지
- 잘못된 JSON·enum·UUID의 400 응답
- 알 수 없는 존 거절과 패스포트 중복 방지
- 대화 시작 멱등성·세션 소유권·3턴 제한
- 동시 메시지 요청에서도 최대 3턴만 저장
- Anthropic 정상 응답, 다국어 문맥, 규칙 기반 대체 응답
- 프론트엔드 빌드와 린트 통과
- 백엔드 전체 테스트 통과

## 6. 운영 설정

| 환경 변수 | 설명 | 기본값 |
|---|---|---|
| `ENTRY_AI_MOCK` | 모의 AI 사용 여부 | `true` |
| `ANTHROPIC_API_KEY` | 실제 Anthropic API 키 | 없음 |
| `ENTRY_AI_MODEL` | 호출할 모델 식별자 | `claude-sonnet-5` |
| `ENTRY_AI_TIMEOUT_SECONDS` | AI 응답 제한 시간 | `8` |

API 키가 없거나 호출이 실패해도 애플리케이션은 기동하고 규칙 기반 응답을 사용한다.
