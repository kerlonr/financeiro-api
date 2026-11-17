FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build

# Imagem do nginx que roda sem root (porta 8080).
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY proxy-api.inc /etc/nginx/conf.d/proxy-api.inc
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 8080
