# ENTRY

매장에서 스캔한 제품과 구매 고민을 익명으로 기록하고, 오프라인에서 시작된 관심을 온라인 아카이브와 후속 상담으로 이어 주는 모바일 웹 서비스입니다.

## 현재 구현 상태

2026년 8월 21일 기준입니다.

| 구분 | 구현 내용 | 상태 |
|---|---|---|
| 공통 | 익명 세션 발급·자동 교체, 공통 응답·오류 형식 | 완료 |
| P1 | 제품 조회, 스캔 기록, 재고·제작 배경 표시, 아카이브 저장 | 완료 |
| P2 백엔드 | 패스포트 발급·조회, 존 스탬프 적립 | 완료 |
| P3 백엔드 | 방문 리캡 조회, 계정 연결 모의 처리 | 완료 |
| P4 백엔드 | 시장별 재고와 국경 이전 정보 조회 | 완료 |
| P5 백엔드 | 아카이브 저장·삭제·조회 | 완료 |
| P6 | 구매 고민 3턴 대화, Anthropic 연동·모의 응답·규칙 기반 대체 응답 | 완료 |
| P2~P5 화면 | 패스포트·리캡·국경 이전·아카이브 화면 | 개발 예정 |
| P7·P8·D1·D2 | 어드바이저·사전 등록·운영자 화면 | 해커톤 우선순위에 따라 선택 구현 |

현재 브라우저에서 확인할 수 있는 주요 경로는 다음과 같습니다.

- `/` — API 연결 상태 확인
- `/s/SKY-STREAM-W260` — P1 스캔 결과
- `/talk/{scanId}` — P6 구매 고민 대화
- `/dev/kitchen-sink` — 공통 컴포넌트 확인
- `/swagger-ui.html` — 백엔드 API 문서

## 기술 구성

- 프론트엔드: React 19, TypeScript 6, Vite 8, Tailwind CSS 4, Zustand, Motion
- 백엔드: Java 21, Spring Boot 3.5.16, Spring MVC, Spring Data JPA, Gradle
- 데이터베이스: 로컬 H2, 배포 PostgreSQL
- AI: Anthropic Messages API, 모의 응답과 규칙 기반 대체 응답 지원

## 로컬 실행

### 준비 사항

- Node.js 20 이상
- npm
- JDK 21

### 프론트엔드

```bash
cd web
npm install
npm run dev
```

프론트엔드는 기본적으로 `http://localhost:5173`에서 실행됩니다.

### 백엔드

macOS·Linux:

```bash
cd api
./gradlew bootRun
```

Windows:

```powershell
cd api
.\gradlew.bat bootRun
```

백엔드는 기본적으로 `http://localhost:8080`에서 실행됩니다. 프론트엔드와 백엔드를 함께 실행하려면 터미널 두 개를 사용하는 것이 가장 확실합니다.

## 환경 변수

프론트엔드는 `web/.env.example`, 백엔드는 `api/.env.example`을 참고합니다.

| 변수 | 용도 | 기본값 |
|---|---|---|
| `VITE_API_BASE` | 프론트엔드가 호출할 API 주소 | `http://localhost:8080/api/v1` |
| `ENTRY_AI_MOCK` | AI 모의 응답 사용 여부 | `true` |
| `ANTHROPIC_API_KEY` | 실제 Anthropic API 키 | 없음 |
| `ENTRY_AI_MODEL` | Anthropic 모델 식별자 | `claude-sonnet-5` |
| `ENTRY_AI_TIMEOUT_SECONDS` | AI 응답 제한 시간 | `8` |
| `ENTRY_CORS_ORIGINS` | 허용할 프론트엔드 출처 | `http://localhost:5173` |

실제 AI를 사용하려면 `ENTRY_AI_MOCK=false`와 `ANTHROPIC_API_KEY`를 백엔드 환경에 설정합니다. 키가 없거나 AI 호출이 실패해도 규칙 기반 응답으로 화면 흐름을 유지합니다.

## 검사 명령어

```bash
cd web
npm run lint
npm run build
```

```bash
cd api
./gradlew clean test
```

## 팀 협업

`main` 브랜치에는 직접 커밋하지 않습니다. Git 설치 없이 GitHub 웹 화면만 사용해도 개인 브랜치를 만들고 PR을 보낼 수 있습니다.

팀원 정보는 같은 줄을 동시에 수정해 충돌이 나지 않도록 각자 별도 파일로 작성합니다.

- 작성 위치: `docs/team/이름.md`
- 작성 내용: 이름, 담당 파트, 한 줄 소개
- 자세한 방법: [팀원 참여 안내](./docs/team/README.md)
- 공통 작업 규칙: [기여 안내](./CONTRIBUTING.md)

## 문서

- [프로젝트 룰북](./CLAUDE.md)
- [제품 요구사항](./docs/PRD.md)
- [API 계약](./docs/API_CONTRACT.md)
- [디자인 시스템](./docs/DESIGN_SYSTEM.md)
- [개발·배포 현황](./BUILD_STEPS.md)
- [다음 작업용 프롬프트](./PROMPTS.md)
