FROM node:22-slim AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# VITE_API_BASE는 Vite 빌드 시점에 번들에 박히는 값이라 build-arg로 받는다.
# docker compose에서 build.args로 전달한다 (docker-compose.yml 참고).
ARG VITE_API_BASE=/api/v1
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
