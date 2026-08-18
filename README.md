# ENTRY

매대 태그를 스캔하면 로그인 없이 그 순간의 구매 의도가 기록되고, 고객이 어느 나라로 이동하든 그 기록이 따라가 현지 재고·채널로 연결되는 서비스.

## 로컬 실행

```bash
npm install
npm --prefix web install
npm run dev
```

`npm run dev`가 웹(5173)·API(8080)를 동시에 띄운다. 웹 첫 화면(`/`)에서 `/api/v1/health` 응답을 확인할 수 있다.

## 시연 순서

1. `/dev/reset`에서 시나리오 프리셋으로 세션을 원하는 상태로 되돌린다("홍콩 고객 3스캔 상태" 추천).
2. `/dev/qr`에서 QR 시트를 인쇄하거나, 스캔 없이 바로 `/s/{productId}`로 진입해 P1(스캔 결과)을 확인한다.
3. "패스포트에 저장" → `/z/{zoneId}` QR로 구역 검인 → `/passport`(P2)에서 도장과 열람 권한 단계를 보여준다.
4. `/recap`(P3) → `/transfer?market=HK`(P4, 클라이맥스: 카드 전환 + MRZ `MKT<KR→MKT<HK` 1.2초 전환)로 이어간다.
5. 운영자 화면은 `/admin`(D1)과 `/admin/personas`(D2)에서 별도로 보여준다. 네트워크가 끊겨도 `?demo=1`로 화면이 계속 동작한다.

## 문서

- [`CLAUDE.md`](./CLAUDE.md) — 프로젝트 룰북 (절대 규칙 R1~R8)
- [`docs/DESIGN_SYSTEM.md`](./docs/DESIGN_SYSTEM.md) — 시각 규칙
- [`docs/API_CONTRACT.md`](./docs/API_CONTRACT.md) — 프론트/백 API 계약
- [`BUILD_STEPS.md`](./BUILD_STEPS.md) — 구축·배포 단계
