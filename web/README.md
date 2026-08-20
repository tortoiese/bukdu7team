# ENTRY 프론트엔드

ENTRY 고객용 모바일 웹입니다. React와 TypeScript로 구현하며, 기준 화면 크기는 402×874입니다.

## 구현된 화면

- `/` — API 연결 상태
- `/s/:productId` — 제품 스캔 결과
- `/talk/:scanId` — 구매 고민 대화
- `/dev/kitchen-sink` — 디자인 시스템 컴포넌트 확인

## 실행

```bash
npm install
npm run dev
```

`web/.env.example`을 참고해 API 주소를 설정합니다.

```env
VITE_API_BASE=http://localhost:8080/api/v1
```

## 검사

```bash
npm run lint
npm run build
```

## 개발 규칙

- 화면 컴포넌트에서 `fetch`를 직접 호출하지 않고 `src/features`의 API 함수를 사용합니다.
- 공통 API 타입은 `src/types/api.ts`에서 관리합니다.
- 세션 ID는 `localStorage["entry.sid"]`에 저장하며 공통 API 클라이언트가 요청·응답 헤더를 동기화합니다.
- 색상·폰트·간격은 `src/styles/tokens.css`의 디자인 토큰을 사용합니다.
- 사용자 문구는 `src/i18n/ko.json`과 `src/i18n/en.json`에서 관리합니다.
- 새 화면을 추가하면 `src/app/router.tsx`와 루트의 `CLAUDE.md`를 함께 갱신합니다.
