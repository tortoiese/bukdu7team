# AI_SPEC.md — ENTRY AI 기능 명세 v0.3

CLAUDE.md R5(AI는 장식이 아니다)를 만족하는 5개 지점(AI-1~AI-5)의 실제 구현 상태를 기록한다.
전부 `io.entry.ai.AiClient` 인터페이스를 통해 호출하며, `entry.ai.mock=true`(기본값)면
`MockAiClient`가 고정 응답을 반환한다. `mock=false`면 `entry.ai.provider`(기본값 `anthropic`)에 따라
`AnthropicClient`(Anthropic Messages API) 또는 `OpenAiClient`(OpenAI Chat Completions API) 중
하나가 실제로 호출된다 — 어느 쪽을 쓰든 도메인 서비스(`Ai*Service`) 코드는 동일하다.
모든 지점은 실패 시 규칙 기반 폴백을 가진다 — AI가 죽어도 화면은 항상 렌더링된다.

**Provider 전환**: `.env`에서 `ENTRY_AI_MOCK=false` + `ENTRY_AI_PROVIDER=openai` + `OPENAI_API_KEY=...`로 바꾸면
Claude 대신 OpenAI가 호출된다. 모델명은 `ENTRY_AI_MODEL`(Anthropic)/`ENTRY_AI_OPENAI_MODEL`(OpenAI)로 각각 지정.

---

## 상태 요약

| ID | 이름 | 화면 | 구현 | 프롬프트 | 폴백 |
|---|---|---|---|---|---|
| AI-1 | 의도 신호 | P1 (`POST /scans`) | ✅ 완료 | `intent-signal.md` | `RuleIntentAnalyzer` |
| AI-2 (인사) | 캐릭터 인사 | P1 | ✅ 완료 | `greeting.md` | `RuleGreetingService` |
| AI-2 (대화) | 3턴 대화 | P6 | ✅ 완료 | `conversation-system.md` | 고정 문구("직원에게 확인 안내") |
| — | 관심 경향 요약 | P3, P5 | ✅ 완료 | `recap-summary.md` | `RuleArchiveSummaryService` 등 |
| AI-4 | 이전 메시지 | P4 | ✅ 완료 | `transfer-message.md` | `RuleTransferAnswerService` + 고정 rationale |
| AI-5 | 어드바이저 브리핑 | P7 | ✅ 완료 | `advisor-briefing.md` | 규칙 기반 브리핑 문장 |
| — | 페르소나 시뮬레이션 | D2 | ✅ 완료 | `persona-simulation.md` | 고정 A/B 결과 |
| **AI-3** | **언어 자동 전환** | 전 화면 | ❌ **미구현** | (없음) | i18n 정적 사전(ko/en만, zh-Hant/ja는 en으로 폴백) |

---

## AI-1~AI-5, 관심 경향 요약, 페르소나 시뮬레이션 (구현 완료)

공통 구조 (`io.entry.ai` 패키지):
- `AiClient` 인터페이스 — `complete(promptKey, prompt)`
- `AiCallExecutor.callWithFallback(call, parse, fallback)` — 호출 실패든 파싱 실패든 1회 재시도 후 폴백
- `PromptLoader` — `resources/prompts/*.md`를 캐싱해서 읽음
- 각 도메인의 `Ai*Service`(예: `AiIntentService`, `AiGreetingService`, `AiConversationService`, `AiTransferAnswerService`, `AiAdvisorBriefingService`, `AiRecapSummaryService`, `PersonaSimulationService`)가 프롬프트 조립 → 호출 → JSON 파싱 → 실패 시 기존 `Rule*` 서비스로 폴백을 담당

응답 `meta.aiUsed` / `data.*.aiUsed`는 실제 성공 여부를 반영한다(목업 모드에서도 `true` — 목업 자체가 "AI 경로가 정상 작동했다"는 신호이기 때문).

---

## AI-3: 언어 자동 전환 (미구현 — 다음 작업)

### 목표 (CLAUDE.md R7, 8장 4)
- 브랜드 승인 문구(`i18n/ko.json`, `i18n/en.json`)를 우선 사용
- 그 사전에 없는 키(현재 `zh-Hant`, `ja` 전체)만 AI가 번역해서 채운다
- 소재명·라인명(`brand/mcm.json`의 `lines`, `characters`)은 번역하지 않고 원어 유지 — 프롬프트에 "다음 용어는 번역하지 마" 목록을 넣는다
- AI가 만든 번역을 그대로 노출하지 않도록, 승인 문구(정적 사전)가 있으면 항상 그게 우선이라는 걸 코드로 강제한다(현재 `i18n/index.ts`의 `translate()`가 이미 `dict[key] ?? dicts.ko[key] ?? key` 순서라 이 우선순위 골격은 있음 — zh-Hant/ja 사전 자체가 없는 게 문제)

### 왜 아직 없는가
- P1~P8 화면 구현(#4~#9)과 AI 연동(#8, AI-1/2/4/5)이 우선순위가 높았고, AI-3은 프론트 i18n 시스템 + 새 백엔드 엔드포인트가 같이 필요한 별도 작업이라 이번 라운드에서 의도적으로 미룸

### 구현 방향 제안 (착수 시 참고)
1. 백엔드에 `POST /api/v1/i18n/translate` 같은 엔드포인트 추가 — `{ locale, keys: string[] }` 받아서 `{ [key]: string }` 반환
2. `translate-ko-to-locale.md` 프롬프트: ko 원문 + "번역하지 말 것" 용어 목록(브랜드 라인명·캐릭터명) + 대상 locale을 넣고 JSON으로 번역 결과를 받음
3. 프론트: `zh-Hant`/`ja`로 세션 전환 시, 없는 키만 모아 위 엔드포인트를 1회 호출해 로컬(메모리 or localStorage) 캐시에 채움. 요청 실패해도 기존처럼 `en`으로 폴백하면 되므로 화면이 막히지 않음
4. `market` 변경 시(`PATCH /sessions/market`) 프론트에서 트리거하면 자연스럽다

---

## 실행 조건
- `ANTHROPIC_API_KEY` 없이도 `entry.ai.mock=true`(기본값)면 전 화면이 목업 응답으로 정상 동작
- 키를 넣고 `entry.ai.mock=false`로 바꾸면 실제 Claude 호출로 전환 — 두 경우 모두 화면 크래시 없음(CLAUDE.md 6장 AI 호출 규칙)
