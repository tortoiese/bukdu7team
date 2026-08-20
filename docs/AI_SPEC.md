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
| **AI-3** | **언어 자동 전환** | 전 화면 | ⚠️ **정적 번역만 완료, AI 호출 없음** | (없음) | i18n 정적 사전 4종(ko/en/zh-Hant/ja) — 2026-08-21 zh-Hant·ja 직접 번역해서 채움 |

---

## AI-1~AI-5, 관심 경향 요약, 페르소나 시뮬레이션 (구현 완료)

공통 구조 (`io.entry.ai` 패키지):
- `AiClient` 인터페이스 — `complete(promptKey, prompt)`
- `AiCallExecutor.callWithFallback(call, parse, fallback)` — 호출 실패든 파싱 실패든 1회 재시도 후 폴백
- `PromptLoader` — `resources/prompts/*.md`를 캐싱해서 읽음
- 각 도메인의 `Ai*Service`(예: `AiIntentService`, `AiGreetingService`, `AiConversationService`, `AiTransferAnswerService`, `AiAdvisorBriefingService`, `AiRecapSummaryService`, `PersonaSimulationService`)가 프롬프트 조립 → 호출 → JSON 파싱 → 실패 시 기존 `Rule*` 서비스로 폴백을 담당

응답 `meta.aiUsed` / `data.*.aiUsed`는 실제 성공 여부를 반영한다(목업 모드에서도 `true` — 목업 자체가 "AI 경로가 정상 작동했다"는 신호이기 때문).

---

## AI-3: 언어 자동 전환 (정적 번역 완료, AI 호출은 아직 없음)

### 2026-08-21 기준 상태
- `i18n/zh-Hant.json`, `i18n/ja.json`을 새로 만들어 `ko.json`/`en.json`과 키를 1:1로 맞춰 직접 번역해서 채웠다.
  더 이상 `en`으로 폴백하지 않는다 — `i18n/index.ts`의 `dicts`에 4개 로케일이 모두 등록되어 있다
- `MarketLocaleResolver`가 타임존/Accept-Language로 HK→zh-Hant, JP→ja를 이미 정확히 추정하므로,
  실제 화면 전환은 세션 시장만 바뀌면 즉시 동작한다(추가 배선 불필요)
- **다만 이건 정적(사람이 미리 써둔) 번역이라 AI를 호출하지 않는다.** CLAUDE.md R5는 AI-1~AI-5 지점을
  "실제로 호출해서 동작"시키라고 요구하므로, AI-3을 그 5개 지점 중 하나로 심사받을 계획이면 아래
  구현 방향으로 AI 호출 경로를 추가해야 한다. 정적 사전만으로 충분하다면(번역 품질·응답 속도·비용
  면에서는 오히려 이쪽이 유리) 이 상태를 최종으로 둬도 된다 — 제품 판단이 필요해 확정하지 않았다

### 목표 (CLAUDE.md R7, 8장 4) — AI 호출 경로를 추가한다면
- 브랜드 승인 문구(정적 사전 4종)를 항상 우선 사용
- 그 사전에 없는 키(신규 화면 추가로 번역이 아직 없는 키)만 AI가 번역해서 채운다
- 소재명·라인명(`brand/mcm.json`의 `lines`, `characters`)은 번역하지 않고 원어 유지 — 프롬프트에 "다음 용어는 번역하지 마" 목록을 넣는다
- AI가 만든 번역을 그대로 노출하지 않도록, 승인 문구(정적 사전)가 있으면 항상 그게 우선이라는 걸 코드로 강제한다(현재 `i18n/index.ts`의 `translate()`가 이미 `dict[key] ?? dicts.ko[key] ?? key` 순서라 이 우선순위 골격은 있음)

### 구현 방향 제안 (착수 시 참고)
1. 백엔드에 `POST /api/v1/i18n/translate` 같은 엔드포인트 추가 — `{ locale, keys: string[] }` 받아서 `{ [key]: string }` 반환
2. `translate-ko-to-locale.md` 프롬프트: ko 원문 + "번역하지 말 것" 용어 목록(브랜드 라인명·캐릭터명) + 대상 locale을 넣고 JSON으로 번역 결과를 받음
3. 프론트: 정적 사전에 없는 키만 모아 위 엔드포인트를 1회 호출해 로컬(메모리 or localStorage) 캐시에 채움. 요청 실패해도 기존처럼 `en`으로 폴백하면 되므로 화면이 막히지 않음
4. `market` 변경 시(`PATCH /sessions/market`) 프론트에서 트리거하면 자연스럽다

---

## 실행 조건
- `ANTHROPIC_API_KEY` 없이도 `entry.ai.mock=true`(기본값)면 전 화면이 목업 응답으로 정상 동작
- 키를 넣고 `entry.ai.mock=false`로 바꾸면 실제 Claude 호출로 전환 — 두 경우 모두 화면 크래시 없음(CLAUDE.md 6장 AI 호출 규칙)
