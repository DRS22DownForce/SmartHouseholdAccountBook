# --- 依存インストール（キャッシュしやすい） ---
# build context はリポジトリルート（openapi を含めるため）
FROM node:20.19-alpine AS deps
WORKDIR /app
COPY frontend-nextjs/package.json frontend-nextjs/package-lock.json ./
RUN npm ci

# --- ビルド ---
FROM node:20.19-alpine AS builder
WORKDIR /app

# openapi-generator-cli は内部で Java を起動する
RUN apk add --no-cache openjdk17-jre-headless

COPY --from=deps /app/node_modules ./node_modules
COPY frontend-nextjs/ ./
# OpenAPI 仕様（generate:api 用）。通常は ../openapi だが Docker 内では /openapi
COPY openapi /openapi

# NEXT_PUBLIC_* はビルド時にバンドルへ埋め込まれる
ARG NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
ARG NEXT_PUBLIC_AWS_REGION=ap-northeast-1
ARG NEXT_PUBLIC_COGNITO_USER_POOL_ID=
ARG NEXT_PUBLIC_COGNITO_CLIENT_ID=
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL \
    NEXT_PUBLIC_AWS_REGION=$NEXT_PUBLIC_AWS_REGION \
    NEXT_PUBLIC_COGNITO_USER_POOL_ID=$NEXT_PUBLIC_COGNITO_USER_POOL_ID \
    NEXT_PUBLIC_COGNITO_CLIENT_ID=$NEXT_PUBLIC_COGNITO_CLIENT_ID \
    NEXT_TELEMETRY_DISABLED=1

# 生成コードは gitignore のためビルド時に作る
RUN npx openapi-generator-cli generate \
      -i /openapi/openapi.yaml \
      -g typescript-axios \
      -o ./src/api/generated \
    && npm run build

# --- 実行（standalone） ---
FROM node:20.19-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0

# 非 root で実行
RUN addgroup --system --gid 1001 nodejs \
    && adduser --system --uid 1001 nextjs

# standalone(next.jsの最小パッケージ出力モード) 出力をコピー（next.config の output: 'standalone' が必要）
COPY --from=builder --chown=nextjs:nodejs /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs
EXPOSE 3000

CMD ["node", "server.js"]
