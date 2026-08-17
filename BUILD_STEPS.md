# BUILD_STEPS.md — ENTRY 구축·배포 단계

VS Code + Claude Code 기준. 각 단계 끝에 **커밋하고 다음 단계로 넘어간다.** 한 세션에서 여러 단계를 몰아서 시키면 품질이 무너진다.

---

## Phase 0 — 환경 준비 (사람이 직접, 30분)

**설치**
- Node.js 20 LTS 이상, JDK 21 (Temurin 권장), Git
- VS Code 확장: `Extension Pack for Java`, `Spring Boot Extension Pack`, `Tailwind CSS IntelliSense`, `ESLint`, `Prettier`, `REST Client` 또는 `Thunder Client`
- Claude Code: `npm i -g @anthropic-ai/claude-code` 후 프로젝트 루트에서 `claude` 실행

**계정**
- GitHub (private repo)
- Vercel (프론트), Railway (백엔드), Neon (Postgres) — 셋 다 GitHub 로그인으로 연결
- Anthropic Console에서 API 키 발급

**리포 초기화**
```bash
mkdir entry && cd entry && git init
mkdir -p docs web api
# 제공된 CLAUDE.md → 루트, DESIGN_SYSTEM.md / API_CONTRACT.md → docs/
# PRD 두 개도 docs/PRD.md, docs/AI_SPEC.md 로 저장
printf ".env\n.env.local\n*.log\nbuild/\ndist/\n.gradle/\nnode_modules/\n" > .gitignore
git add . && git commit -m "chore: 룰북과 제품 문서 배치"
```

> **중요**: 문서를 먼저 커밋한 뒤에 Claude Code를 처음 띄운다. 코드가 없는 상태에서 룰북을 읽히는 게 가장 효과가 크다.

---

## Phase 1 — 스캐폴딩 (프롬프트 #1, 약 20분)

- `web/`: Vite React-TS, Tailwind v4, Router, Zustand, motion, @fontsource 3종
- `api/`: Spring Boot 3.3 / Java 21 / Gradle KTS, Web·JPA·Validation·springdoc, H2 로컬 프로필
- 루트 `package.json`에 `dev` 스크립트(concurrently로 둘 동시 실행)
- 헬스체크: `GET /api/v1/health` → 프론트 첫 화면에서 표시

**완료 조건**: `npm run dev` 한 번으로 5173(웹)·8080(API)이 뜨고, 웹 화면에 API 응답이 보인다.

---

## Phase 2 — 디자인 시스템 (프롬프트 #2, 약 40분)

`tokens.css` + Tailwind `@theme` 매핑 + 공통 컴포넌트 8종(Button, Field, HairlineSection, MrzBar, Stamp, GuillocheBg, StatusBar, Toast) + `/dev/kitchen-sink` 라우트.

**완료 조건**: 킷친싱크에서 스탬프가 찍히고, MRZ 밴드가 값 변경 시 스크램블되고, `prefers-reduced-motion`에서 정적으로 보인다.

> 이 단계를 건너뛰면 이후 모든 화면이 제각각 스타일로 나온다. **가장 아끼면 안 되는 단계.**

---

## Phase 3 — 백엔드 도메인 + 더미 데이터 (프롬프트 #3, 약 50분)

- 익명 세션, 제품, 스캔, 아카이브, 패스포트 엔티티/서비스/컨트롤러
- `resources/seed/`: `products.json`(20종), `zones.json`(4개), `inventory.json`(4개 시장), `personas.json`(5종)
- `InventoryPort` 인터페이스 + `DummyInventoryAdapter`
- 의도 해석은 **먼저 규칙 기반으로만** 구현 (AI는 Phase 5에서 얹는다)
- springdoc으로 `/swagger-ui.html` 노출

**완료 조건**: Swagger에서 세션 발급 → 스캔 → 저장 → 패스포트 조회가 순서대로 성공한다.

---

## Phase 4 — P0 화면 4개 (프롬프트 #4~#7, 약 2~3시간)

순서를 지킨다. **P4를 먼저 만들고 싶은 유혹을 참는다** — P1이 없으면 데이터가 안 쌓인다.

1. P1 스캔 결과 (프롬프트 #4)
2. P2 팝업 패스포트 (프롬프트 #5)
3. P3 방문 리캡 (프롬프트 #6)
4. P4 국경 이전 (프롬프트 #7) ← 여기에 시간을 가장 많이 쓴다

**완료 조건**: QR 없이도 `/s/SKY-STREAM-W260` 직접 진입 → 저장 → `/passport` → `/recap` → `/transfer`가 한 흐름으로 이어진다.

---

## Phase 5 — AI 연동 (프롬프트 #8, 약 1시간)

- `AnthropicClient`(WebClient, 타임아웃 8초, 재시도 1회)
- `resources/prompts/`: `intent-signal.md`, `greeting.md`, `recap-summary.md`, `transfer-message.md`, `advisor-briefing.md`, `conversation-system.md`
- `entry.ai.mock` 플래그, 모든 지점 폴백 경로
- 응답에 `meta.aiUsed` 채우기

**완료 조건**: API 키를 지운 상태에서도 전 화면이 폴백으로 정상 렌더된다. 키를 넣으면 문장이 달라진다.

---

## Phase 6 — 남은 화면 (프롬프트 #9~#10, 약 2시간)

P6 대화 → P8 사전 등록 → P5 아카이브 → D1 대시보드 → P7 어드바이저 → D2 페르소나 콘솔.
시간이 부족하면 **D2와 P7을 버린다.** P6과 D1은 심사에서 질문받을 확률이 높으므로 살린다.

---

## Phase 7 — QR·데모 자산 (프롬프트 #11, 약 30분)

- `/dev/qr` 라우트: 제품 20종 + 구역 4개의 QR을 A4 인쇄용 시트로 생성 (`qrcode` 패키지, 클라이언트 렌더)
- QR은 배포된 프론트 URL을 가리켜야 한다. 로컬 IP로 굽지 않는다
- 시나리오 리셋 버튼(`/dev/reset`)으로 세션을 초기화해 리허설을 반복 가능하게 한다

---

## Phase 8 — 배포

### 8-1. DB (Neon)
프로젝트 생성 → connection string 확보 (`postgresql://…?sslmode=require`)

### 8-2. 백엔드 (Railway)
`api/Dockerfile`
```dockerfile
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["sh","-c","java $JAVA_OPTS -jar app.jar"]
```
Railway → New Project → Deploy from GitHub → Root directory `api` → 환경변수:

| 키 | 값 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://…` |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | Neon 값 |
| `ANTHROPIC_API_KEY` | sk-ant-… |
| `ENTRY_CORS_ORIGINS` | `https://entry-xxx.vercel.app` |
| `PORT` | Railway가 주입 → `server.port=${PORT:8080}` 로 받는다 |

배포 후 `https://…railway.app/api/v1/health` 확인.

### 8-3. 프론트 (Vercel)
Import GitHub repo → Root Directory `web` → Framework Vite → 환경변수 `VITE_API_BASE=https://…railway.app`
SPA 라우팅용 `web/vercel.json`
```json
{ "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }] }
```

### 8-4. 연결 점검 체크리스트
- [ ] CORS: 브라우저 콘솔에 preflight 오류 없음
- [ ] HTTPS 혼용 없음 (프론트 https → API도 https)
- [ ] 세션 발급이 시크릿 창에서도 성공
- [ ] 모바일 실기기에서 QR 스캔 → P1 진입
- [ ] 홍콩 시장 시뮬레이션: 기기 언어를 중문으로 바꿔 진입 → 언어·통화 전환 확인
- [ ] Railway 콜드스타트 대비: 발표 10분 전 헬스체크 1회 호출

---

## Phase 9 — 데모 리허설

- 발표 순서대로 3회 실행 (문제 40s → 공백 30s → 솔루션 60s → 시연 120s → 검증 40s → ROI 30s)
- **네트워크 폴백**: 화면 녹화 mp4를 준비하고, `?demo=1`로 API 없이 목 데이터로 도는 모드를 남긴다
- 시연은 국경 이전 화면의 MRZ가 `MKT<KR` → `MKT<HK`로 바뀌는 순간에서 멈춘다

---

## 시간 배분 권장 (36시간 해커톤 기준)

| 구간 | 시간 | 누적 |
|---|---|---|
| Phase 0–2 | 3h | 3h |
| Phase 3 | 2h | 5h |
| Phase 4 | 6h | 11h |
| Phase 5 | 2h | 13h |
| Phase 6 | 4h | 17h |
| Phase 7–8 | 3h | 20h |
| 버퍼·리허설·발표자료 | 8h | 28h |
| 예비 | 8h | 36h |

배포를 마지막에 몰지 않는다. **Phase 2가 끝난 직후 한 번 배포해서 파이프라인을 먼저 뚫어둔다.**
