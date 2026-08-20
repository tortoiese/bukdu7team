# API_CONTRACT.md — ENTRY 프론트/백엔드 계약

버전 `v1`, 2026년 8월 21일 기준. 이 문서에는 **현재 백엔드에 구현된 API만** 기록한다. 엔드포인트를 바꾸면 같은 커밋에서 이 문서를 갱신한다. 향후 기능은 `PRD.md`에서 관리하며 구현 전에는 이 계약에 포함하지 않는다.

## 구현 현황

| 영역 | API | 프론트 연결 |
|---|---|---|
| 세션 | 완료 | 완료 |
| 제품·스캔 | 완료 | P1 연결 완료 |
| 아카이브 | 완료 | P1 저장 연결 완료, 목록 화면 예정 |
| 패스포트 | 완료 | 화면 예정 |
| 방문 리캡 | 완료 | 화면 예정 |
| 국경 이전 | 완료 | 화면 예정 |
| 구매 고민 대화 | 완료 | P6 연결 완료 |

---

## 0. 공통 규약

- 기본 주소: `{API_BASE}/api/v1`
- 세션이 필요한 요청 헤더: `X-Entry-Session: <uuid>`
- 예외 경로: `GET /health`, `POST /sessions`
- 응답 래퍼

```json
{ "data": { }, "meta": { "requestId": "…", "aiUsed": true, "fallback": false } }
```

- 에러

```json
{ "error": { "code": "PRODUCT_NOT_FOUND", "message": "제품을 찾을 수 없습니다." } }
```

- `market` 열거형: `KR | HK | JP | US`
- `locale` 열거형: `ko | en | zh-Hant | ja`
- 시각은 전부 ISO-8601 UTC
- **세션이 유효하지 않으면 401을 반환하지 않고 새 세션을 발급해 `meta.sessionRotated=true`로 알린다**
- 세션이 필요한 모든 응답은 실제 처리에 사용한 세션 ID를 `X-Entry-Session` 응답 헤더로 반환한다. 프론트는 이 값을 즉시 `localStorage["entry.sid"]`에 저장하며 별도 세션을 재발급하지 않는다.

### 공통 오류 코드

| 상태 | 코드 | 의미 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 필수값·길이·동의 조건 오류 |
| 400 | `INVALID_REQUEST_BODY` | JSON 또는 열거형 본문 오류 |
| 400 | `INVALID_PARAMETER` | 경로·쿼리 파라미터 변환 오류 |
| 400 | `INVALID_SCAN_ID` | 아카이브 `scanId` UUID 형식 오류 |
| 400 | `INVALID_ZONE` | 서버 카탈로그에 없는 존 |
| 404 | `PRODUCT_NOT_FOUND` | 제품 없음 |
| 404 | `SCAN_NOT_FOUND` | 현재 세션의 스캔 없음 |
| 404 | `CONVERSATION_NOT_FOUND` | 현재 세션의 대화 없음 |
| 404 | `PASSPORT_NOT_FOUND` | 패스포트 미발급 |
| 409 | `ZONE_ALREADY_STAMPED` | 이미 검인한 존 |
| 429 | `STAMP_TOO_SOON` | 이전 검인 후 60초 미경과 |
| 500 | `INTERNAL_ERROR` | 예상하지 못한 서버 오류 |

---

## 1. 세션

### `POST /sessions`
익명 세션 발급. 저장된 세션이 없을 때 프론트 부팅 과정에서 호출한다.

요청
```json
{ "acceptLanguage": "zh-HK,zh;q=0.9", "timezone": "Asia/Hong_Kong", "entryPoint": "STORE_TAG" }
```

응답 `data`
```json
{
  "sessionId": "b1f2…",
  "market": "HK",
  "locale": "zh-Hant",
  "marketInferred": true,
  "createdAt": "2026-08-18T04:12:00Z"
}
```

### `PATCH /sessions/market`
사용자가 시장/언어를 수동 변경.
요청 `{ "market": "JP", "locale": "ja" }` → `data`: 세션 객체

### `GET /sessions/current`
로컬에 저장된 세션을 화면 요청 전에 확정한다. 세션이 무효하면 서버가 새 세션을 발급하고, `data.sessionId`와 `X-Entry-Session` 응답 헤더에 같은 ID를 반환한다. 프론트는 이 요청이 끝난 뒤에만 스캔·제품 조회를 시작한다.

### `GET /health`
세션 없이 서버 상태를 확인한다.

응답 `data`:

```json
{ "status": "UP", "version": "dev", "profile": "local" }
```

---

## 2. 제품 · 스캔 (P1)

### `GET /products/{productId}?market=HK`
응답 `data`
```json
{
  "productId": "SKY-STREAM-W260",
  "line": "Sky Stream",
  "displayName": "스카이 스트림 백팩",
  "material": "코티드 캔버스",
  "weightGram": 780,
  "sizeLabel": { "local": "26cm", "origin": "260" },
  "craftNotes": [
    { "heading": "실루엣 의도", "body": "…" },
    { "heading": "소재 선택", "body": "…" }
  ],
  "media": [],
  "stock": {
    "thisStore": "IN_STOCK",
    "domesticOther": [{ "storeId": "KR-HDS", "storeName": "현대 무역센터", "status": "IN_STOCK" }],
    "homeMarket": { "market": "HK", "status": "IN_STOCK", "storeName": "코즈웨이베이" }
  },
  "priceDisplay": null
}
```

`stock.*.status` 열거형: `IN_STOCK | TRANSFERABLE | ONLINE_ONLY | OUT_OF_STOCK`
`priceDisplay`는 **항상 null 또는 미노출**. 할인 관련 필드를 추가하지 않는다.

### `POST /scans`
스캔 1건 기록. 저장(save)과는 별개 이벤트.

요청
```json
{ "productId": "SKY-STREAM-W260", "storeId": "KR-SEONGSU", "zoneId": "ZONE03", "scannedAt": "…" }
```

응답 `data`
```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "scanCountForProduct": 3,
  "sessionScanCount": 7,
  "intentSignal": {
    "stage": "SIZE_DECIDED",
    "comparisonAxis": "COLOR",
    "unresolved": "COLOR_CARE",
    "confidence": 0.72,
    "rationale": "같은 라인 안에서 컬러만 바꿔 3회 스캔했습니다.",
    "aiUsed": false
  },
  "greeting": {
    "character": "KAISER",
    "message": "스카이 스트림, 세 번째 보시네요. 컬러 때문인가요, 사이즈 때문인가요?"
  }
}
```

`stage` 열거형: `BROWSING | CATEGORY_COMPARE | LINE_COMPARE | SIZE_DECIDED | READY`
`unresolved` 열거형: `SIZE | COLOR_CARE | PORTABILITY | CAPACITY | GIFT_FIT | UNKNOWN`
`character` 열거형: `HARU | HENRY | KAISER`

현재 스캔 의도와 인사말은 규칙 기반으로 생성하므로 `intentSignal.aiUsed`는 `false`다.

---

## 3. 아카이브 (P5)

### `POST /archive`
요청 `{ "productId": "…", "scanId": "…" }` → `data { "savedCount": 7 }`

### `DELETE /archive/{productId}` → `data { "savedCount": 6 }`

### `GET /archive?market=HK`
```json
{
  "items": [
    { "productId": "…", "displayName": "…", "savedAt": "…",
      "savedAtStoreId": "KR-SEONGSU", "zoneId": "ZONE03",
      "thumbnail": "…", "homeMarketStatus": "IN_STOCK" }
  ],
  "intentSummary": { "text": "Sky Stream 라인을 중심으로 코티드 캔버스 소재 제품을 1개 저장했습니다.", "aiUsed": false }
}
```

---

## 4. 패스포트 (P2)

### `POST /passport` — 입구 스캔 시 발급
요청 `{ "popupId": "MCM-SEONGSU-2026", "issuedAtStore": "KR-SEONGSU" }`

### `POST /passport/stamps`
요청 `{ "zoneId": "ZONE03" }`
응답 `data`
```json
{
  "zoneId": "ZONE03",
  "stampedAt": "…",
  "rotationSeed": 41,
  "accessTier": 2,
  "tierUnlocked": true,
  "nextTier": { "tier": 3, "requirement": "전 구역 방문", "remainingZones": ["ZONE04"] }
}
```
`rotationSeed`는 서버가 고정 발급 → 스탬프 각도가 리렌더마다 변하지 않게 한다.

### `GET /passport`
```json
{
  "passportNo": "ENT-KR-B1F2A7C48D194DA9A44AA1974E79A601",
  "issuedAt": "…", "issuedPlace": "SEOUL / SEONGSU",
  "popupId": "MCM-SEONGSU-2026",
  "zones": [
    { "zoneId": "ZONE01", "name": "HERITAGE", "visited": true, "stampedAt": "…", "rotationSeed": 12 },
    { "zoneId": "ZONE04", "name": "PRODUCT", "visited": false }
  ],
  "savedCount": 7,
  "accessTier": 2,
  "grants": [{ "code": "ARCHIVE_VIEW", "label": "팝업 한정 아카이브 열람", "active": true }],
  "mrz": ["ENTRY<<SEOUL<SEONGSU<<ZONE03<<<<", "SAVED07<<INTENT<COLOR<<MKT<KR>HK<<"]
}
```
MRZ 문자열은 **서버가 생성**해 내려준다. 프론트가 조립하지 않는다.

어뷰징 방지: 동일 세션의 동일 구역 재스탬프는 무효이며, 서로 다른 스탬프 사이에는 최소 60초 간격을 적용한다. 현재 MVP는 서버의 존 카탈로그 검증을 사용하고 `zoneToken` 서명은 구현하지 않았다.

---

## 5. 방문 리캡 (P3)

### `GET /recap`
```json
{
  "visitDate": "2026-08-18",
  "storeName": "성수 팝업",
  "viewedProducts": [{ "productId": "…", "displayName": "…", "scanCount": 3, "order": 1 }],
  "interestSummary": { "text": "…", "aiUsed": false },
  "unresolvedFactors": [{ "code": "COLOR_CARE", "label": "화이트 컬러 관리", "productId": "…" }],
  "accountLink": { "required": false, "reason": "기록을 잃지 않기 위해 연락 수단을 연결할 수 있습니다." }
}
```

### `POST /recap/link`
해커톤용 계정 연결 모의 처리다. `channel`, `value`, `consent=true`를 검증하지만 연락처 원문은 저장하지 않는다.

요청 `{ "channel": "EMAIL", "value": "…" , "consent": true }` → `data { "linked": true }`

---

## 6. 국경 이전 (P4) — 핵심

### `GET /transfer?market=HK`
```json
{
  "originStore": "SEOUL / SEONGSU",
  "targetMarket": "HK",
  "currency": "HKD",
  "generatedAt": "…",
  "sendTiming": { "recommendedAt": "2026-08-21T02:00:00Z", "rationale": "마지막 확인 시점에서 72시간 뒤로 제안합니다.", "aiUsed": false },
  "items": [
    { "productId": "SKY-STREAM-W260", "displayName": "…",
      "status": "IN_STOCK", "storeName": "코즈웨이베이",
      "action": { "type": "RESERVE", "label": "매장 방문 예약" } },
    { "productId": "PM-TOTE", "displayName": "…",
      "status": "TRANSFERABLE", "transferDays": 4, "fromStore": "KR-SEONGSU",
      "action": { "type": "REQUEST_TRANSFER", "label": "이동 요청" } },
    { "productId": "DENIM-01", "displayName": "…",
      "status": "ONLINE_ONLY",
      "action": { "type": "ONLINE", "label": "온라인 스토어에서 주문", "url": "…" } }
  ],
  "unresolvedAnswers": [
    { "code": "COLOR_CARE", "question": "밝은 컬러 관리가 어렵지 않을까 고민이 있었습니다.", "answer": "코팅 소재는 물티슈로 닦아내는 방식으로 관리할 수 있습니다.", "aiUsed": false }
  ],
  "mrzTransition": { "from": "MKT<KR", "to": "MKT<HK" }
}
```

`action.type` 열거형: `RESERVE | REQUEST_TRANSFER | ONLINE | NOTIFY_RESTOCK`
재고 조회는 `resources/seed/inventory.json`의 4개 시장 더미. 실제 연동 지점은 `InventoryPort` 인터페이스로 분리해 두고 `DummyInventoryAdapter`를 주입한다.

---

## 7. 대화 (P6)

### `POST /conversations`
요청 `{ "scanId": "…" }` → `data { "conversationId": "…", "turnsRemaining": 3, "messages": [...] }`

- `scanId`는 현재 세션에서 생성된 스캔이어야 한다. 다른 세션의 스캔은 `SCAN_NOT_FOUND`(404)로 처리한다.
- 같은 스캔으로 다시 요청하면 기존 대화와 메시지를 복원한다.

### `POST /conversations/{conversationId}/messages`
요청 `{ "text": "컬러요. 화이트가 관리 어려울 것 같아서" }`
응답 `data`
```json
{
  "reply": { "character": "KAISER", "message": "코티드 캔버스라 물티슈로 닦입니다. …" },
  "turnsRemaining": 1,
  "extracted": { "criteria": ["관리 용이성"], "unresolved": "COLOR_CARE" },
  "handoffSuggested": false
}
```
- 세 번째 답변부터 `handoffSuggested:true`가 되며, 이후 요청에는 직원 상담 안내만 반환하고 AI를 더 호출하지 않는다
- 캐릭터는 판매하지 않는다. 시스템 프롬프트에 "결정 기준 정리에 한정, 구매 권유 금지" 명시
- `ENTRY_AI_MOCK=false`이고 `ANTHROPIC_API_KEY`가 있을 때만 외부 AI를 호출한다. 타임아웃·응답 형식 오류·키 누락 시 제품 정보 기반 규칙 답변으로 전환하고 `meta.fallback=true`를 반환한다.

---

## 8. 프론트엔드 API 클라이언트 규칙

- `web/src/features/<domain>/api.ts` 에 도메인별 함수. 컴포넌트에서 `fetch` 금지
- 공통 `client.ts`가 세션 요청 헤더 주입, 응답 `X-Entry-Session` 동기화, 에러 → `ApiError` 변환 담당
- 타입은 `web/src/types/api.ts`에 이 문서와 1:1로 정의. 서버 enum과 문자열 리터럴 유니온을 맞춘다
- `VITE_API_BASE` 환경변수 사용. 하드코딩된 localhost 금지

---

## 9. 아직 계약에 포함하지 않은 기능

다음 기능은 PRD에는 있지만 현재 컨트롤러가 없으므로 API 계약에서 제외한다.

- P7 어드바이저 동의·열람
- P8 사전 등록
- D1 운영자 의도 대시보드
- D2 페르소나 콘솔

구현을 시작할 때 요청·응답 구조를 먼저 합의하고 이 문서에 추가한다.
