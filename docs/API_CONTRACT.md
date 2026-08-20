# API_CONTRACT.md — ENTRY 프론트/백엔드 계약

버전 `v1`. 프론트와 백엔드는 이 문서를 유일한 기준으로 삼는다. 엔드포인트를 바꾸면 **같은 커밋에서 이 문서를 갱신**한다.

---

## 0. 공통 규약

- Base URL: `{API_BASE}/api/v1`
- 모든 요청 헤더: `X-Entry-Session: <uuid>` (첫 세션 발급 요청만 예외)
- D1/D2(`/admin/**`, `/personas/**`)는 추가로 `X-Entry-Admin-Token: <uuid>` 헤더 필요(10장 참고)
- 응답 래퍼

```json
{ "data": { }, "meta": { "requestId": "…", "aiUsed": true, "fallback": false } }
```

- 에러

```json
{ "error": { "code": "PRODUCT_NOT_FOUND", "message": "제품을 찾을 수 없습니다." } }
```

- `market` enum: `KR | HK | JP | US | SG` (SG는 2026-08-21 추가 — 실제 MCM 매장이 있는 도시 기준. locale은 `en`을 공유한다)
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

### `GET /products`
`/dev/qr` QR 시트 전용 가벼운 목록. 재고 조회 없음.
Response `data`
```json
[{ "productId": "SKY-STREAM-W260", "displayName": "스카이 스트림 백팩 화이트 260", "line": "Sky Stream" }, ...]
```

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
스캔 1건 기록. 저장(save)과는 별개 이벤트. 세션에 패스포트가 아직 없으면 이 `storeId`로 조용히
발급된다(4장 참고) — 매장별 QR 시트(`/dev/qr`)가 `?store=`로 보내는 값이 여기로 그대로 들어온다.

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

**실제 게스트 흐름에는 "발급 버튼"이 없다.** `POST /scans`(P1 매대 태그)든 `POST /passport/stamps`(P4
구역 검인)든, 세션의 첫 스캔이 곧 입구 태그를 댄 것과 같아 그 순간 조용히·멱등하게 발급된다.
`issuedAtStore`가 매장 카탈로그(성수/더현대/합정/문래, 2026-08-21 추가)에 없으면 성수 팝업으로
폴백한다 — 기존에 뿌려둔 QR과 호환된다. 이미 발급된 세션이면 이후 storeId는 무시하고 기존
패스포트를 그대로 돌려준다.

### `POST /passport` — 개발용 수동 발급(정상 플로우에서는 쓰지 않는다, `/dev/reset` 전용)
Request `{ "popupId": "MCM-SEONGSU-2026", "issuedAtStore": "KR-SEONGSU" }`
`issuedAtStore`가 실제 발급 장소/popupId를 결정한다 — `popupId` 필드값은 무시된다.

### `POST /passport/stamps`
Request `{ "zoneId": "ZONE03", "storeId": "KR-HYUNDAI" }` — `storeId`는 선택값, 없으면 성수로 폴백
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
Request `{ "channel": "EMAIL", "value": "…" , "consent": true }` → `data { "linked": true, "emailSent": true }`

`value`(원문 이메일/전화번호)는 DB에 저장하지 않는다 — SHA-256 해시만 남는다(CLAUDE.md R8).
`channel`이 `EMAIL`이고 `consent`가 true면, 그 요청 안에서만 잠깐 쓴 원문 이메일로 저장 링크
(`{ENTRY_WEB_ORIGIN}/resume/{sessionId}`)를 1회 발송한다 — 발송 실패해도 `linked`는 그대로 true,
`emailSent`만 false. `entry.mail.mock=true`(기본값)면 실제로 보내지 않고 서버 로그에만 남긴다.
`channel`이 `PHONE`이면 `emailSent`는 항상 false.

### `GET /resume/:sessionId` (프론트 전용 라우트, API 아님)
이메일로 받은 링크. `localStorage["entry.sid"]`를 그 sessionId로 설정하고 `/passport`로 이동한다 —
세션 ID를 아는 사람은 누구나 그 세션에 접근할 수 있다는 기존 신뢰 모델을 그대로 확장한 것이다.

### `POST /recap/lookup` (2026-08-21 추가 — 이메일로 돌아가기, `/lookup`)
`X-Entry-Session` 없이도 호출 가능. Request `{ "email": "…" }` → 입력값을 해시해 기존 `RecapLink`
해시와 대조한다(원문 대조 아님 — DB에 원문이 없으므로). 같은 이메일로 여러 번 연결했다면 가장 최근
세션을 돌려준다.
Response `data { "sessionId": "…" }`. 못 찾으면 404 `CONTACT_NOT_FOUND`.
프론트는 이 sessionId를 `localStorage["entry.sid"]`에 심고 `/passport`로 이동, 방금 입력한
이메일 문자열은 (서버 조회 결과가 아니라 클라이언트에 남긴 것으로) 화면 상단에 계속 표시한다.

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

**D1/D2(`/admin/**`, `/personas/**`)는 `X-Entry-Admin-Token` 헤더가 없으면 전부 401
`ADMIN_AUTH_REQUIRED`를 반환한다.** 게스트 세션(`X-Entry-Session`)과 별도 층위이며, 세션처럼
자동 재발급되지 않는다 — 프론트는 401을 받으면 토큰을 지우고 `/entryadmin`으로 보낸다.

### `POST /admin/login`
관리자 인증. `X-Entry-Admin-Token` 없이 호출 가능(로그인 자체는 예외).
Request `{ "password": "entryadmin" }`
Response `data { "adminToken": "…", "expiresAt": "2026-08-21T00:00:00Z" }`
실패 시 401 `{ "error": { "code": "ADMIN_PASSWORD_INVALID", "message": "…" } }`
`adminToken`은 이후 모든 D1/D2 요청에 `X-Entry-Admin-Token` 헤더로 첨부한다.

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
