# ENTRY 백엔드 안정화 및 P6 대화 기능 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기존 P1 데이터 흐름의 세션·검증·패스포트 오류를 수정하고, P1의 대화 링크를 실제 P6 구매 고민 대화 API와 모바일 화면에 연결한다.

**Architecture:** 기존 도메인 패키지를 유지하면서 안정화 수정은 해당 도메인 안에서 최소화한다. 새 `conversation` 도메인은 세션 소유권·3턴 제한·메시지 저장을 담당하고, `ConversationAiClient` 뒤에 모의·Anthropic·규칙 폴백 구현을 분리한다. 프론트는 공통 API 클라이언트로 세션 회전을 처리하고 별도 conversation feature를 통해서만 서버를 호출한다.

**Tech Stack:** Java 21, Spring Boot 3.5.16, Spring MVC, WebClient, Spring Data JPA, H2/PostgreSQL, React 19, TypeScript 6, Vite 8, Zustand

**Spec:** `docs/superpowers/specs/2026-08-21-backend-stabilization-conversation-design.md`

## Global Constraints

- 기존 라이브러리만 사용하고 새 의존성을 추가하지 않는다.
- 익명 세션을 유지하며 PII 원문과 API 키를 프론트 또는 DB에 저장하지 않는다.
- 할인·쿠폰·구매 강요 문구를 만들지 않는다.
- 모든 백엔드 응답은 기존 `ApiResponse<T>` 또는 `ApiErrorResponse` 계약을 유지한다.
- 모든 새 텍스트는 한국어·영어 i18n 파일을 통해 제공한다.
- 프롬프트는 `api/src/main/resources/prompts/*.md`에서 로드한다.
- 기능 구현 전에 실패 테스트를 추가하고 실패 원인을 확인한다.
- 커밋·푸시는 사용자가 별도로 승인하기 전에는 실행하지 않는다.

---

### Task 1: 세션 자동 교체를 현재 요청과 동일한 세션으로 연결

**Files:**
- Create: `api/src/test/java/io/entry/session/SessionRotationIntegrationTest.java`
- Modify: `api/src/main/java/io/entry/session/SessionInterceptor.java`
- Modify: `api/src/main/java/io/entry/common/CorsConfig.java`
- Modify: `web/src/features/client.ts`
- Modify: `web/src/features/session/store.ts`
- Modify: `docs/API_CONTRACT.md`

**Interfaces:**
- Produces: 응답 헤더 `X-Entry-Session`, `SessionState.replaceSession(sessionId: string): void`
- Consumes: `SessionService.findValid`, `SessionService.autoIssue`, `RequestContext.setSessionId`

- [ ] **Step 1: 무효 세션 회전 통합 테스트 작성**

```java
@Test
void 무효_세션으로_스캔하면_응답_헤더의_새_세션에_기록된다() throws Exception {
    MvcResult result = mvc.perform(post("/api/v1/scans")
            .header("X-Entry-Session", "invalid-session")
            .contentType(APPLICATION_JSON)
            .content("""
                {"productId":"SKY-STREAM-W260","storeId":"KR-SEONGSU","zoneId":"ZONE04"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.sessionRotated").value(true))
        .andReturn();

    String rotated = result.getResponse().getHeader("X-Entry-Session");
    assertThat(rotated).isNotBlank();
    assertThat(scanEventRepository.findBySessionIdOrderByScannedAtAsc(UUID.fromString(rotated))).hasSize(1);
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `cd api && ./gradlew test --tests '*SessionRotationIntegrationTest'`
Expected: FAIL — 응답의 `X-Entry-Session` 헤더가 없음

- [ ] **Step 3: 서버가 확정 세션을 응답 헤더로 노출**

```java
AnonymousSession resolved = session != null ? session : sessionService.autoIssue();
boolean rotated = session == null;
String sessionId = resolved.getId().toString();
RequestContext.setSessionId(sessionId, rotated);
response.setHeader(HEADER, sessionId);
```

`CorsConfig`에는 다음을 추가한다.

```java
configuration.setExposedHeaders(List.of(SessionInterceptor.HEADER));
```

- [ ] **Step 4: 프론트가 응답 헤더를 즉시 저장하도록 변경**

```ts
const rotatedSessionId = res.headers.get('X-Entry-Session')
if (rotatedSessionId) useSessionStore.getState().replaceSession(rotatedSessionId)
```

```ts
replaceSession(sessionId) {
  localStorage.setItem(STORAGE_KEY, sessionId)
  set({ sessionId })
}
```

`meta.sessionRotated`에서 `reissue()`를 호출하던 코드는 제거한다.

- [ ] **Step 5: 계약 문서와 테스트 갱신 후 통과 확인**

Run: `cd api && ./gradlew test --tests '*SessionRotationIntegrationTest'`
Expected: PASS

---

### Task 2: 잘못된 요청을 일관된 400 응답으로 변환

**Files:**
- Create: `api/src/test/java/io/entry/common/InputValidationIntegrationTest.java`
- Modify: `api/src/main/java/io/entry/common/GlobalExceptionHandler.java`
- Modify: `api/src/main/java/io/entry/archive/ArchiveService.java`
- Modify: `api/src/main/java/io/entry/common/EntryException.java`

**Interfaces:**
- Produces: `EntryException.badRequest(String code, String message)`
- Consumes: 기존 `ApiErrorResponse.of`

- [ ] **Step 1: enum·JSON·scanId 오류 테스트 작성**

```java
@Test
void 잘못된_market은_400이다() throws Exception {
    mvc.perform(get("/api/v1/archive?market=INVALID").header(SESSION, sessionId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
}

@Test
void 잘못된_scanId는_400이다() throws Exception {
    mvc.perform(post("/api/v1/archive").header(SESSION, sessionId)
            .contentType(APPLICATION_JSON)
            .content("""{"productId":"SKY-STREAM-W260","scanId":"not-a-uuid"}"""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("INVALID_SCAN_ID"));
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `cd api && ./gradlew test --tests '*InputValidationIntegrationTest'`
Expected: FAIL — 500 응답

- [ ] **Step 3: 공통 입력 예외 핸들러 추가**

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
ResponseEntity<ApiErrorResponse> handleUnreadable() {
    return ResponseEntity.badRequest().body(ApiErrorResponse.of("INVALID_REQUEST_BODY", "요청 본문을 확인해주세요."));
}

@ExceptionHandler(MethodArgumentTypeMismatchException.class)
ResponseEntity<ApiErrorResponse> handleTypeMismatch() {
    return ResponseEntity.badRequest().body(ApiErrorResponse.of("INVALID_PARAMETER", "요청 파라미터를 확인해주세요."));
}
```

- [ ] **Step 4: ArchiveService UUID 파싱을 도메인 오류로 변환**

```java
private UUID parseScanId(String raw) {
    try {
        return UUID.fromString(raw);
    } catch (IllegalArgumentException ex) {
        throw EntryException.badRequest("INVALID_SCAN_ID", "scanId 형식을 확인해주세요.");
    }
}
```

- [ ] **Step 5: 검증 테스트 통과 확인**

Run: `cd api && ./gradlew test --tests '*InputValidationIntegrationTest'`
Expected: PASS

---

### Task 3: 패스포트 존과 발급 무결성 보강

**Files:**
- Create: `api/src/test/java/io/entry/passport/PassportIntegrityIntegrationTest.java`
- Modify: `api/src/main/java/io/entry/catalog/ZoneCatalog.java`
- Modify: `api/src/main/java/io/entry/passport/Passport.java`
- Modify: `api/src/main/java/io/entry/passport/PassportService.java`

**Interfaces:**
- Produces: `Zone ZoneCatalog.require(String zoneId)`, 세션 UUID 기반 `passportNo`
- Consumes: `EntryException.badRequest`, `PassportRepository.findBySessionId`

- [ ] **Step 1: 알 수 없는 존과 반복 발급 테스트 작성**

```java
@Test
void 존재하지_않는_존은_적립되지_않는다() throws Exception {
    issuePassport(sessionId);
    mvc.perform(post("/api/v1/passport/stamps").header(SESSION, sessionId)
            .contentType(APPLICATION_JSON).content("""{"zoneId":"ZONE99"}"""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("INVALID_ZONE"));
    assertThat(stampRepository.count()).isZero();
}

@Test
void 같은_세션의_패스포트_반복_발급은_같은_번호다() {
    assertThat(service.issue(sessionId, "MCM-SEONGSU-2026").passportNo())
        .isEqualTo(service.issue(sessionId, "MCM-SEONGSU-2026").passportNo());
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `cd api && ./gradlew test --tests '*PassportIntegrityIntegrationTest'`
Expected: FAIL — ZONE99가 저장됨

- [ ] **Step 3: 카탈로그 존 검증 추가**

```java
public Zone require(String zoneId) {
    Zone zone = byId.get(zoneId == null ? null : zoneId.trim().toUpperCase(Locale.ROOT));
    if (zone == null) throw EntryException.badRequest("INVALID_ZONE", "존 코드를 확인해주세요.");
    return zone;
}
```

- [ ] **Step 4: 패스포트 번호와 DB 제약 변경**

```java
@Table(name = "passport", uniqueConstraints = {
    @UniqueConstraint(columnNames = "sessionId"),
    @UniqueConstraint(columnNames = "passportNo")
})
```

```java
String passportNo = "ENT-KR-" + sessionId.toString().replace("-", "").toUpperCase(Locale.ROOT);
```

- [ ] **Step 5: 패스포트 테스트 통과 확인**

Run: `cd api && ./gradlew test --tests '*PassportIntegrityIntegrationTest'`
Expected: PASS

---

### Task 4: P6 대화 저장 모델과 3턴 서비스 구현

**Files:**
- Create: `api/src/main/java/io/entry/conversation/Conversation.java`
- Create: `api/src/main/java/io/entry/conversation/ConversationMessage.java`
- Create: `api/src/main/java/io/entry/conversation/ConversationRepository.java`
- Create: `api/src/main/java/io/entry/conversation/ConversationMessageRepository.java`
- Create: `api/src/main/java/io/entry/conversation/ConversationController.java`
- Create: `api/src/main/java/io/entry/conversation/ConversationService.java`
- Create: `api/src/main/java/io/entry/conversation/dto/ConversationStartRequest.java`
- Create: `api/src/main/java/io/entry/conversation/dto/ConversationStartData.java`
- Create: `api/src/main/java/io/entry/conversation/dto/ConversationMessageRequest.java`
- Create: `api/src/main/java/io/entry/conversation/dto/ConversationReplyData.java`
- Create: `api/src/main/java/io/entry/conversation/ai/ConversationAiService.java`
- Create: `api/src/main/java/io/entry/conversation/ai/AiConversationContext.java`
- Create: `api/src/main/java/io/entry/conversation/ai/AiConversationReply.java`
- Create: `api/src/test/java/io/entry/conversation/ConversationIntegrationTest.java`

**Interfaces:**
- Produces: `ConversationService.start(UUID, UUID)`, `ConversationService.reply(UUID, UUID, String)`
- Consumes: `ScanEventRepository`, `ProductCatalog`

- [ ] **Step 1: 시작 멱등성과 세션 소유권 테스트 작성**

```java
@Test
void 같은_스캔의_대화_시작은_멱등적이다() {
    var first = service.start(sessionId, scanId);
    var second = service.start(sessionId, scanId);
    assertThat(second.conversationId()).isEqualTo(first.conversationId());
}

@Test
void 다른_세션의_스캔으로_대화를_시작할_수_없다() {
    assertThatThrownBy(() -> service.start(otherSessionId, scanId))
        .isInstanceOfSatisfying(EntryException.class, ex -> assertThat(ex.code()).isEqualTo("SCAN_NOT_FOUND"));
}
```

- [ ] **Step 2: 3턴 제한 테스트 작성**

```java
@Test
void 세_턴_후에는_AI를_호출하지_않고_직원_연결을_제안한다() {
    UUID conversationId = service.start(sessionId, scanId).conversationId();
    service.reply(sessionId, conversationId, "첫 질문");
    service.reply(sessionId, conversationId, "두 번째 질문");
    service.reply(sessionId, conversationId, "세 번째 질문");
    var fourth = service.reply(sessionId, conversationId, "네 번째 질문");
    assertThat(fourth.turnsRemaining()).isZero();
    assertThat(fourth.handoffSuggested()).isTrue();
    verify(aiService, times(3)).reply(any());
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `cd api && ./gradlew test --tests '*ConversationIntegrationTest'`
Expected: FAIL — conversation 패키지 없음

- [ ] **Step 4: 엔티티와 저장소 구현**

`Conversation`은 `(sessionId, scanId)` 유니크, `turnCount`, `productId`, `character`, 생성·수정 시각을 저장한다. `ConversationMessage`는 `conversationId`, `role`, 최대 500자 `text`, 생성 시각을 저장한다.

- [ ] **Step 5: 대화 AI 포트의 최소 규칙 구현**

`ConversationAiService.reply(AiConversationContext)`가 제품 사실과 입력 키워드로 `AiConversationReply`를 반환하게 만든다. 이 단계에서는 `aiUsed=false`, `fallback=true`로 고정하고 Task 5에서 실제 클라이언트 선택과 장애 폴백을 추가한다.

- [ ] **Step 6: 서비스와 컨트롤러 최소 구현**

```java
public ConversationReplyData reply(UUID sessionId, UUID conversationId, String text) {
    Conversation conversation = repository.findByIdAndSessionId(conversationId, sessionId)
        .orElseThrow(() -> EntryException.notFound("CONVERSATION_NOT_FOUND", "대화를 찾을 수 없습니다."));
    if (conversation.getTurnCount() >= 3) return handoff(conversation);
    messages.save(ConversationMessage.user(conversation.getId(), text));
    AiConversationReply reply = aiService.reply(context(conversation, text));
    conversation.increaseTurn();
    repository.save(conversation);
    messages.save(ConversationMessage.character(conversation.getId(), reply.message()));
    return toData(conversation, reply);
}
```

- [ ] **Step 7: 대화 테스트 통과 확인**

Run: `cd api && ./gradlew test --tests '*ConversationIntegrationTest'`
Expected: PASS

---

### Task 5: Anthropic·모의·규칙 폴백 AI 구현

**Files:**
- Create: `api/src/main/java/io/entry/conversation/ai/ConversationAiClient.java`
- Modify: `api/src/main/java/io/entry/conversation/ai/ConversationAiService.java`
- Create: `api/src/main/java/io/entry/conversation/ai/MockConversationAiClient.java`
- Create: `api/src/main/java/io/entry/conversation/ai/AnthropicConversationAiClient.java`
- Create: `api/src/main/java/io/entry/conversation/ai/RuleConversationFallback.java`
- Modify: `api/src/main/java/io/entry/conversation/ai/AiConversationContext.java`
- Modify: `api/src/main/java/io/entry/conversation/ai/AiConversationReply.java`
- Create: `api/src/main/resources/prompts/conversation.md`
- Create: `api/src/test/java/io/entry/conversation/ai/ConversationAiServiceTest.java`
- Modify: `api/src/main/resources/application.yml`
- Modify: `api/.env.example`

**Interfaces:**
- Produces: `AiConversationReply ConversationAiService.reply(AiConversationContext context)`
- Consumes: `ConversationAiClient.generate`, `RuleConversationFallback.generate`

- [ ] **Step 1: AI 성공·실패 폴백 테스트 작성**

```java
@Test
void 클라이언트_실패_시_규칙_응답을_반환한다() {
    when(client.generate(context)).thenThrow(new RuntimeException("timeout"));
    AiConversationReply reply = service.reply(context);
    assertThat(reply.fallback()).isTrue();
    assertThat(reply.aiUsed()).isFalse();
    assertThat(reply.message()).isNotBlank();
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `cd api && ./gradlew test --tests '*ConversationAiServiceTest'`
Expected: FAIL — AI 서비스 타입 없음

- [ ] **Step 3: 인터페이스와 규칙 폴백 구현**

```java
public interface ConversationAiClient {
    AiConversationReply generate(AiConversationContext context);
}
```

`COLOR_CARE`, `SIZE`, `PORTABILITY`, `CAPACITY`, `GIFT_FIT` 키워드를 기존 `UnresolvedCode`에 매핑하고 제품 카탈로그 사실만 답변에 사용한다.

- [ ] **Step 4: 모의 클라이언트 구현**

`@ConditionalOnProperty(name="entry.ai.mock", havingValue="true", matchIfMissing=true)`로 활성화하고 고정 JSON 의미의 `AiConversationReply`를 반환한다.

- [ ] **Step 5: Anthropic WebClient 구현**

`@ConditionalOnProperty(name="entry.ai.mock", havingValue="false")`로 활성화한다. `x-api-key`, `anthropic-version`, `content-type` 헤더를 서버에서 설정하고 `entry.ai.timeout-seconds`를 적용한다. 응답 텍스트를 구조화 JSON으로 파싱하며 파싱 실패 시 즉시 규칙 기반 답변으로 전환한다.

- [ ] **Step 6: 프롬프트와 설정 추가**

프롬프트에는 구매 강요 금지, 할인 제안 금지, 제공된 제품 사실만 사용, `message/criteria/unresolved` JSON 출력 규칙을 명시한다.

- [ ] **Step 7: AI 테스트 통과 확인**

Run: `cd api && ./gradlew test --tests '*ConversationAiServiceTest'`
Expected: PASS

---

### Task 6: P6 프론트 화면과 API 연결

**Files:**
- Create: `web/src/features/conversation/api.ts`
- Create: `web/src/screens/Talk.tsx`
- Modify: `web/src/app/router.tsx`
- Modify: `web/src/types/api.ts`
- Modify: `web/src/i18n/ko.json`
- Modify: `web/src/i18n/en.json`

**Interfaces:**
- Produces: `startConversation(scanId)`, `sendConversationMessage(conversationId, text)`
- Consumes: 기존 `apiRequest`, `ConversationStart`, `ConversationReply`, `MobileFrame`, `CharacterBubble`, `Button`

- [ ] **Step 1: 타입과 API 함수 추가**

```ts
export function startConversation(scanId: string) {
  return apiRequest<ConversationStart>('/conversations', { method: 'POST', body: { scanId } })
}

export function sendConversationMessage(conversationId: string, text: string) {
  return apiRequest<ConversationReply>(`/conversations/${conversationId}/messages`, {
    method: 'POST', body: { text },
  })
}
```

- [ ] **Step 2: Talk 화면 구현**

화면은 `scanId`로 대화를 시작하고 메시지 배열, 입력값, 전송 중 상태, 오류 상태를 관리한다. 전송 성공 시 사용자 메시지와 캐릭터 답변을 추가하고 `turnsRemaining`을 갱신한다. 0턴이면 입력을 비활성화하고 직원 연결 안내를 표시한다.

- [ ] **Step 3: 라우트와 i18n 연결**

```tsx
{ path: '/talk/:scanId', element: <Talk /> }
```

한국어·영어에 제목, 입력 placeholder, 전송, 남은 대화, 재시도, 직원 연결 문구를 추가한다.

- [ ] **Step 4: 프론트 빌드와 린트 확인**

Run: `npm --prefix web run build && npm --prefix web run lint`
Expected: 두 명령 모두 exit 0

---

### Task 7: 계약 동기화와 전체 회귀 검증

**Files:**
- Modify: `docs/API_CONTRACT.md`
- Modify: `docs/PRD.md`
- Modify: `README.md`
- Modify: `api/src/test/java/io/entry/EntryFlowIntegrationTest.java`

**Interfaces:**
- Consumes: Task 1~6의 모든 공개 API
- Produces: 프론트·백 계약과 실행 가능한 시연 절차

- [ ] **Step 1: API 계약 갱신**

세션 응답 헤더 규칙과 `/conversations`, `/conversations/{id}/messages` 요청·응답·오류 코드를 문서에 반영한다.

- [ ] **Step 2: 전체 사용자 흐름 테스트 작성**

```java
@Test
void 세션부터_스캔_저장_대화까지_이어진다() throws Exception {
    String session = createSession();
    String scanId = scan(session, "SKY-STREAM-W260");
    save(session, "SKY-STREAM-W260", scanId);
    String conversationId = startConversation(session, scanId);
    reply(session, conversationId, "화이트 컬러 관리가 고민돼요");
    assertThat(savedItemRepository.countBySessionId(UUID.fromString(session))).isEqualTo(1);
    assertThat(conversationMessageRepository.findByConversationIdOrderByCreatedAtAsc(UUID.fromString(conversationId))).hasSize(3);
}
```

- [ ] **Step 3: 백엔드 전체 검증**

Run: `cd api && ./gradlew clean test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 프론트 전체 검증**

Run: `npm --prefix web run build && npm --prefix web run lint`
Expected: 두 명령 모두 exit 0

- [ ] **Step 5: 변경 범위와 비밀값 검사**

Run: `git diff --check && git status --short && rg -n 'ANTHROPIC_API_KEY=.+|console\.log' api web docs`
Expected: diff 오류와 실제 API 키 없음. 새 `console.log` 없음.
