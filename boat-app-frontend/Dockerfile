# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Copy dependency manifests first for better Docker layer caching
COPY package*.json ./
RUN npm ci

# Copy source and build for production
COPY . .
RUN npm run build -- --configuration=production

# ─── Stage 2: Serve ──────────────────────────────────────────────────────────
FROM nginx:alpine

# Copy compiled Angular app
COPY --from=builder /app/dist/boat-app-frontend/browser /usr/share/nginx/html

# Copy SPA-aware nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

