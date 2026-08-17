# API_CONTRACT.md — ENTRY 프론트/백엔드 계약

버전 `v1`. 프론트와 백엔드는 이 문서를 유일한 기준으로 삼는다. 엔드포인트를 바꾸면 **같은 커밋에서 이 문서를 갱신**한다.

---

## 0. 공통 규약

- Base URL: `{API_BASE}/api/v1`
- 모든 요청 헤더: `X-Entry-Session: <uuid>` (첫 세션 발급 요청만 예외)
- 응답 래퍼

```json
{ "data": { }, "meta": { "requestId": "…", "aiUsed": true, "fallback": false } }
```

- 에러

```json
{ "error": { "code": "PRODUCT_NOT_FOUND", "message": "제품을 찾을 수 없습니다." } }
```

- `market` enum: `KR | HK | JP | US`
- `locale` enum: `ko | en | zh-Hant | ja`
- 시각은 전부 ISO-8601 UTC
- **세션이 유효하지 않으면 401을 반환하지 않고 새 세션을 발급해 `meta.sessionRotated=true`로 알린다**

---

## 1. 세션

### `POST /sessions`
익명 세션 발급. 프론트 부팅 시 1회.

Request
```json
{ "acceptLanguage": "zh-HK,zh;q=0.9", "timezone": "Asia/Hong_Kong", "entryPoint": "STORE_TAG" }
```

Response `data`
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
Request `{ "market": "JP", "locale": "ja" }` → `data`: 세션 객체

---

## 2. 제품 · 스캔 (P1)

### `GET /products/{productId}?market=HK`
Response `data`
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
  "media": ["/assets/products/sky-stream-w260-01.jpg"],
  "stock": {
    "thisStore": "IN_STOCK",
    "domesticOther": [{ "storeId": "KR-HDS", "storeName": "현대 무역센터", "status": "IN_STOCK" }],
    "homeMarket": { "market": "HK", "status": "IN_STOCK", "storeName": "코즈웨이베이" }
  },
  "priceDisplay": null
}
```

`stock.*.status` enum: `IN_STOCK | TRANSFERABLE | ONLINE_ONLY | OUT_OF_STOCK`
`priceDisplay`는 **항상 null 또는 미노출**. 할인 관련 필드를 추가하지 않는다.

### `POST /scans`
스캔 1건 기록. 저장(save)과는 별개 이벤트.

Request
```json
{ "productId": "SKY-STREAM-W260", "storeId": "KR-SEONGSU", "zoneId": "ZONE03", "scannedAt": "…" }
```

Response `data`
```json
{
  "scanId": "sc_01H…",
  "scanCountForProduct": 3,
  "sessionScanCount": 7,
  "intentSignal": {
    "stage": "SIZE_DECIDED",
    "comparisonAxis": "COLOR",
    "unresolved": "COLOR_CARE",
    "confidence": 0.72,
    "rationale": "같은 라인 안에서 컬러만 바꿔 3회 스캔했습니다.",
    "aiUsed": true
  },
  "greeting": {
    "character": "KAISER",
    "message": "스카이 스트림, 세 번째 보시네요. 컬러 때문인가요, 사이즈 때문인가요?"
  }
}
```

`stage` enum: `BROWSING | CATEGORY_COMPARE | LINE_COMPARE | SIZE_DECIDED | READY`
`unresolved` enum: `SIZE | COLOR_CARE | PORTABILITY | CAPACITY | GIFT_FIT | UNKNOWN`
`character` enum: `HARU | HENRY | KAISER`

> AI 실패 시 `aiUsed:false`, 규칙 기반 값으로 채우고 `rationale`은 관측 사실만 서술.

---

## 3. 아카이브 (P5)

### `POST /archive`
Request `{ "productId": "…", "scanId": "…" }` → `data { "savedCount": 7 }`

### `DELETE /archive/{productId}` → `data { "savedCount": 6 }`

### `GET /archive?market=HK`
```json
{
  "items": [
    { "productId": "…", "displayName": "…", "savedAt": "…",
      "savedAt Store": "KR-SEONGSU", "zoneId": "ZONE03",
      "thumbnail": "…", "homeMarketStatus": "IN_STOCK" }
  ],
  "intentSummary": { "text": "블랙 계열, 노트북 수납, 경량 소재를 중심으로 보셨습니다.", "aiUsed": true }
}
```
필드명에 공백을 쓰지 않는다 — 실제 구현은 `savedAtStoreId`.

---

## 4. 패스포트 (P2)

### `POST /passport` — 입구 스캔 시 발급
Request `{ "popupId": "MCM-SEONGSU-2026", "issuedAtStore": "KR-SEONGSU" }`

### `POST /passport/stamps`
Request `{ "zoneId": "ZONE03" }`
Response `data`
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
  "passportNo": "ENT-KR-0002841",
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

어뷰징 방지: 동일 세션 동일 구역 재스탬프 무효, 스탬프 간 최소 간격 60초, `zoneId`는 시간 기반 회전 코드로 서명 (`POST /passport/stamps` 에 `zoneToken` 추가 가능).

---

## 5. 방문 리캡 (P3)

### `GET /recap`
```json
{
  "visitDate": "2026-08-18",
  "storeName": "성수 팝업",
  "viewedProducts": [{ "productId": "…", "displayName": "…", "scanCount": 3, "order": 1 }],
  "interestSummary": { "text": "…", "aiUsed": true },
  "unresolvedFactors": [{ "code": "COLOR_CARE", "label": "화이트 컬러 관리", "productId": "…" }],
  "accountLink": { "required": false, "reason": "기록을 잃지 않기 위해 연락 수단을 연결할 수 있습니다." }
}
```

### `POST /recap/link` (계정 연결 — 해커톤에서는 이메일 형식 검증만, 저장은 해시 처리)
Request `{ "channel": "EMAIL", "value": "…" , "consent": true }` → `data { "linked": true }`

---

## 6. 국경 이전 (P4) — 핵심

### `GET /transfer?market=HK`
```json
{
  "originStore": "SEOUL / SEONGSU",
  "targetMarket": "HK",
  "currency": "HKD",
  "generatedAt": "…",
  "sendTiming": { "recommendedAt": "2026-08-21T02:00:00Z", "rationale": "체류 마지막 날 이후로 판단했습니다.", "aiUsed": true },
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
    { "code": "COLOR_CARE", "question": "화이트 관리가 어렵지 않을까", "answer": "코티드 캔버스라 물티슈로 닦입니다.", "aiUsed": true }
  ],
  "mrzTransition": { "from": "MKT<KR", "to": "MKT<HK" }
}
```

`action.type` enum: `RESERVE | REQUEST_TRANSFER | ONLINE | NOTIFY_RESTOCK`
재고 조회는 `resources/seed/inventory.json`의 4개 시장 더미. 실제 연동 지점은 `InventoryPort` 인터페이스로 분리해 두고 `DummyInventoryAdapter`를 주입한다.

---

## 7. 대화 (P6)

### `POST /conversations`
Request `{ "scanId": "…" }` → `data { "conversationId": "…", "turnsRemaining": 3, "messages": [...] }`

### `POST /conversations/{id}/messages`
Request `{ "text": "컬러요. 화이트가 관리 어려울 것 같아서" }`
Response `data`
```json
{
  "reply": { "character": "KAISER", "message": "코티드 캔버스라 물티슈로 닦입니다. …" },
  "turnsRemaining": 1,
  "extracted": { "criteria": ["관리 용이성"], "unresolved": "COLOR_CARE" },
  "handoffSuggested": false
}
```
- 3턴 초과 시 `handoffSuggested:true`, 직원 호출 안내로 전환하고 더 이상 생성하지 않는다
- 캐릭터는 판매하지 않는다. 시스템 프롬프트에 "결정 기준 정리에 한정, 구매 권유 금지" 명시

---

## 8. 어드바이저 (P7)

### `POST /consents/advisor`
고객이 공유 범위 선택 → 일회용 열람 토큰 발급
Request `{ "scope": ["SAVED_ITEMS", "UNRESOLVED"], "ttlSeconds": 900 }`
Response `data { "grantToken": "…", "expiresAt": "…", "qrPayload": "https://…/advisor/…" }`

### `GET /advisor/{grantToken}`
```json
{
  "briefing": { "text": "스카이 스트림 화이트 260을 세 번 확인. 사이즈 확정, 컬러 관리가 미해결. 홍콩 거주.", "aiUsed": true },
  "savedItems": [...], "unresolved": [...], "locale": "zh-Hant",
  "keyPhrases": [{ "ko": "코티드 캔버스", "target": "塗層帆布" }],
  "expiresAt": "…"
}
```

### `POST /advisor/{grantToken}/notes`
Request `{ "note": "…" }` → 고객 타임라인에 저장. 만료 후 401 + `GRANT_EXPIRED`

---

## 9. 사전 등록 (P8)

### `POST /preregistrations`
Request `{ "channel": "EMAIL", "value": "…", "interestedLines": ["SKY_STREAM"], "market": "HK", "consent": true }`
Response `data { "slot": "PRIORITY_ENTRY", "timeWindow": "11:00–12:00", "code": "PRE-2841" }`
할인·쿠폰 필드를 만들지 않는다.

---

## 10. 대시보드 (D1)

### `GET /admin/intent-dashboard?popupId=…&from=…&to=…`
```json
{
  "products": [{ "productId": "…", "scans": 412, "rescanRate": 0.31, "saveRate": 0.44, "conversionRate": 0.06 }],
  "unresolvedDistribution": [{ "code": "SIZE", "count": 180 }, { "code": "COLOR_CARE", "count": 121 }],
  "marketDistribution": [{ "market": "HK", "sessions": 220 }],
  "transferRecovery": { "sent": 140, "converted": 19, "rate": 0.136 },
  "zonePerformance": [{ "zoneId": "ZONE01", "avgDwellSeconds": 214, "saves": 61 }],
  "actionHints": ["SIZE 미해결이 집중된 3개 제품에 실측 정보를 태그에 추가하세요."]
}
```

---

## 11. 페르소나봇 콘솔 (D2)

### `GET /personas` → 5종 목록
### `POST /personas/{id}/simulate`
Request `{ "hypothesis": "H3", "variantA": "INFO_LIST", "variantB": "CONVERSATIONAL", "productId": "…" }`
Response `data`
```json
{
  "runId": "…",
  "results": [{ "variant": "CONVERSATIONAL", "saved": true, "reason": "…", "unresolved": "COLOR_CARE" }],
  "disclaimer": "페르소나봇 응답은 실제 고객 행동이 아니라 대중 인식의 평균치입니다. 방향 판단에만 사용합니다."
}
```
`disclaimer`는 서버가 항상 포함하고 UI에서 숨기지 않는다.

---

## 12. 프론트 API 클라이언트 규칙

- `web/src/features/<domain>/api.ts` 에 도메인별 함수. 컴포넌트에서 `fetch` 금지
- 공통 `client.ts`가 세션 헤더 주입, `meta.sessionRotated` 처리, 에러 → `ApiError` 변환 담당
- 타입은 `web/src/types/api.ts`에 이 문서와 1:1로 정의. 서버 enum과 문자열 리터럴 유니온을 맞춘다
- `VITE_API_BASE` 환경변수 사용. 하드코딩된 localhost 금지
