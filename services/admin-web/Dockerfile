FROM node:20-alpine AS builder

WORKDIR /app
COPY . .
RUN yarn install --frozen-lockfile
RUN NODE_OPTIONS=--openssl-legacy-provider yarn build

FROM nginx:stable-alpine

COPY --from=builder /app/dist /data/www
COPY --from=builder /app/etc/nginx /etc/nginx
COPY etc/nginx/templates /etc/nginx/templates
COPY docker-entrypoint.d/19-validate-api-url.sh /docker-entrypoint.d/19-validate-api-url.sh

ENV API_URL=http://api:8080

EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
