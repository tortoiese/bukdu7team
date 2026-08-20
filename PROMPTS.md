# ENTRY 다음 작업용 프롬프트

초기 프로젝트 생성용 프롬프트는 현재 구현과 맞지 않아 제거했습니다. 아래 프롬프트는 2026년 8월 21일의 코드 상태를 기준으로 남은 작업을 이어 갈 때 사용합니다.

한 번에 하나의 화면 또는 하나의 도메인만 작업합니다. 작업 전 계획을 확인하고, 완료 후 검사 결과와 변경 파일을 공유합니다.

## 공통 시작 문구

```text
먼저 CLAUDE.md, docs/PRD.md, docs/DESIGN_SYSTEM.md, docs/API_CONTRACT.md를 읽어줘.
현재 구현 상태는 README.md와 BUILD_STEPS.md를 기준으로 확인해줘.
API 계약과 현재 코드를 대조한 다음 작업 계획을 5줄 이내로 먼저 말해줘.
main 브랜치에는 직접 커밋하지 말고 현재 작업 브랜치에서만 수정해줘.
새 라이브러리는 추가하지 말고 기존 컴포넌트와 API를 우선 재사용해줘.
완료 후 변경 파일, 실행 방법, 검사 결과만 한국어로 정리해줘.
```

## 1. P2 팝업 패스포트 화면

```text
[공통 시작 문구]

기존 패스포트 API를 사용해 /passport 화면을 구현해줘.

- GET /api/v1/passport로 기존 패스포트를 조회
- 없으면 POST /api/v1/passport로 발급
- POST /api/v1/passport/stamps로 존 스탬프 적립
- 존 4개의 방문 여부, 접근 등급, 열람 권한, MRZ 표시
- 중복 스탬프와 60초 제한 오류를 사용자 행동 안내 문구로 표시
- MobileFrame, Stamp, ProgressGauge, MrzBar 등 기존 컴포넌트 재사용
- 문구는 ko.json과 en.json에 추가

npm run lint와 npm run build까지 확인해줘.
```

## 2. P3 방문 리캡 화면

```text
[공통 시작 문구]

기존 리캡 API를 사용해 /recap 화면을 구현해줘.

- GET /api/v1/recap으로 방문 제품, 관심 요약, 미해결 요인 표시
- POST /api/v1/recap/link로 계정 연결을 모의 처리
- 실제 이메일·전화번호를 화면 밖에 저장하지 않기
- 계정 연결은 선택 사항이며 핵심 기록 열람을 막지 않기
- AI 대체 응답이어도 화면 구조가 유지되게 처리
- 기존 모바일 컴포넌트와 디자인 토큰 재사용

npm run lint와 npm run build까지 확인해줘.
```

## 3. P4 국경 이전 화면

```text
[공통 시작 문구]

기존 국경 이전 API를 사용해 /transfer 화면을 구현해줘.

- GET /api/v1/transfer?market=HK 호출
- 출발 매장, 대상 시장, 통화, 제품별 현지 재고와 행동 표시
- MRZ의 시장 코드가 KR에서 HK로 바뀌는 전환을 시연의 핵심으로 구성
- 할인·최저가·가격 비교 문구를 사용하지 않기
- AI가 만든 답변과 규칙 기반 답변 모두 같은 화면에 표시
- prefers-reduced-motion 환경에서는 즉시 최종 상태 표시

npm run lint와 npm run build까지 확인해줘.
```

## 4. P5 아카이브 화면

```text
[공통 시작 문구]

기존 아카이브 API를 사용해 /archive 화면을 구현해줘.

- GET /api/v1/archive?market=KR로 저장 목록 조회
- DELETE /api/v1/archive/{productId}로 저장 해제
- 제품명, 저장 시각, 저장 매장, 존, 본국 시장 재고 상태 표시
- 관심 요약과 AI 사용 여부를 과장 없이 표현
- 빈 상태에서 P1 스캔 화면으로 이동할 수 있게 안내

npm run lint와 npm run build까지 확인해줘.
```

## 5. 핵심 사용자 흐름 통합

```text
[공통 시작 문구]

P1, P2, P3, P4, P5, P6 화면을 하나의 시연 흐름으로 연결해줘.

- 스캔 결과 → 아카이브 저장 → 구매 고민 대화
- 패스포트 → 방문 리캡 → 국경 이전
- 각 화면에서 뒤로 가기와 다음 단계 이동 경로 점검
- 무효 세션 교체 직후 중복 세션이 생기지 않는지 확인
- 로딩, 오류, 빈 상태에서 사용자가 막히지 않게 처리
- 모바일 402×874와 실제 휴대전화 브라우저를 우선 기준으로 사용

새 기능을 추가하기보다 현재 API와 화면의 연결 완성도를 우선해줘.
```

## 6. 배포 준비

```text
[공통 시작 문구]

현재 저장소를 Vercel, Railway, Neon에 배포할 수 있게 점검해줘.

- 비밀값은 코드에 넣지 않고 환경 변수로만 관리
- Vercel SPA 새로고침 설정 확인
- Railway가 api 폴더를 Java 21로 빌드할 수 있는지 확인
- PostgreSQL 운영 프로필 확인
- 프론트엔드 VITE_API_BASE와 백엔드 ENTRY_CORS_ORIGINS 연결
- /api/v1/health와 Swagger 접근 확인 절차 문서화

배포 파일을 수정하기 전 변경 이유와 영향을 먼저 알려줘.
```

## 7. PR 검토 요청

```text
현재 브랜치와 main의 차이를 코드 리뷰해줘.
치명적인 오류, API 계약 불일치, 세션 소유권 문제, 개인정보·API 키 노출 가능성을 우선 확인해줘.
프론트엔드는 npm run lint와 npm run build, 백엔드는 ./gradlew clean test를 실행해줘.
문제가 있으면 중요도와 파일 위치를 한국어로 정리하고, 수정은 내가 승인한 뒤 진행해줘.
```
