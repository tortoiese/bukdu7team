#!/usr/bin/env bash
# 가비아 VM에서 실행하는 배포 스크립트. 최신 코드를 받아 docker compose로 재빌드/재기동한다.
# 사용법: ./deploy.sh [브랜치명]  (기본값: feature/dev)
set -euo pipefail

BRANCH="${1:-feature/dev}"

echo "==> $BRANCH 브랜치 최신 코드 받는 중..."
git fetch origin
git checkout "$BRANCH"
git pull origin "$BRANCH"

echo "==> Docker 이미지 재빌드 및 재기동..."
docker compose up -d --build

echo "==> 완료. 컨테이너 상태:"
docker compose ps

echo "==> 헬스체크:"
sleep 3
curl -sf http://localhost/api/v1/health && echo || echo "health check 실패 — docker compose logs api 로 확인하세요."
