# Build stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage - Nginx for serving static files
FROM nginx:alpine

# Install envsubst for environment variable substitution
RUN apk add --no-cache gettext

COPY --from=builder /app/dist /usr/share/nginx/html
COPY default.conf.template /etc/nginx/templates/default.conf.template

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget -q --spider http://localhost:80 || exit 1

EXPOSE 80

# Use envsubst to substitute environment variables in nginx config
# Set defaults to Docker service names if env vars not set
CMD export AUTH_SERVICE_URL=${AUTH_SERVICE_URL:-http://auth-service:8085} && \
    export ORDER_SERVICE_URL=${ORDER_SERVICE_URL:-http://order-service:8081} && \
    export INVENTORY_SERVICE_URL=${INVENTORY_SERVICE_URL:-http://inventory-service:8082} && \
    export RECOMMENDATION_SERVICE_URL=${RECOMMENDATION_SERVICE_URL:-http://recommendation-service:8084} && \
    envsubst '$$AUTH_SERVICE_URL $$ORDER_SERVICE_URL $$INVENTORY_SERVICE_URL $$RECOMMENDATION_SERVICE_URL' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && \
    nginx -g 'daemon off;'
